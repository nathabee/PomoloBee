#from django.shortcuts import render

from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.views import APIView
from .models import Field, Fruit, Raw, ImageHistory
from .serializers import FieldSerializer, FruitSerializer,FieldLocationSerializer,\
                    ImageSerializer,ImageUploadSerializer,RawUpdateSerializer,FieldUpdateSerializer,\
                    HistorySerializer, MLResultSerializer
from rest_framework import  status 
from django.core.files.storage import default_storage
from rest_framework.generics import UpdateAPIView
from django.shortcuts import get_object_or_404 
import os

import requests
from django.conf import settings 
    



# Set ML API URL (Flask/FastAPI)
ML_API_URL = os.getenv("ML_API_URL", "http://ml-service:5000")



class FieldViewSet(viewsets.ReadOnlyModelViewSet):  # Only allows GET requests
    queryset = Field.objects.all()
    serializer_class = FieldSerializer

class FruitViewSet(viewsets.ReadOnlyModelViewSet):  # Only allows GET requests
    queryset = Fruit.objects.all()
    serializer_class = FruitSerializer
 

class LocationListView(APIView):
    def get(self, request, format=None):
        fields = Field.objects.prefetch_related('raws__fruit').all()  # Optimized query
        if not fields.exists():
            return Response({"error": "No field and raw data available."}, status=404)

        serializer = FieldLocationSerializer(fields, many=True)
        return Response({"locations": serializer.data}, status=200)
    

# image 
class ImageListView(APIView):
    def get(self, request):
        images = ImageHistory.objects.all()
        serializer = ImageSerializer(images, many=True)
        return Response({"images": serializer.data}, status=status.HTTP_200_OK)

class ImageUploadView(APIView):
    def post(self, request):
        serializer = ImageUploadSerializer(data=request.data)
        if serializer.is_valid():
            image_file = serializer.validated_data['image']
            raw_id = serializer.validated_data['raw_id']
            file_path = default_storage.save(f'images/{image_file.name}', image_file)
            image_history = ImageHistory.objects.create(image_path=file_path, raw_id=raw_id)
            return Response({"image_id": image_history.id, "message": "Image uploaded successfully."}, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class ImageDetailView(APIView):
    def get(self, request, image_id):
        image = get_object_or_404(ImageHistory, id=image_id)
        serializer = ImageSerializer(image)
        return Response(serializer.data, status=status.HTTP_200_OK)

class ImageStatusView(APIView):
    def get(self, request, image_id):
        image = get_object_or_404(ImageHistory, id=image_id)
        return Response({
            "image_id": image.id,
            "status": "done" if image.processed else "processing",
            "processed": image.processed,
            "nb_apfel": image.nb_apfel,
            "confidence_score": image.confidence_score
        }, status=status.HTTP_200_OK)

class ErrorLogView(APIView):
    def get(self, request, image_id):
        image = get_object_or_404(ImageHistory, id=image_id)
        return Response({"error_log": "No errors found."}, status=status.HTTP_200_OK)

class ImageDeleteView(APIView):
    def delete(self, request, image_id):
        image = get_object_or_404(ImageHistory, id=image_id)
        default_storage.delete(image.image_path)
        image.delete()
        return Response({"message": "Image deleted successfully."}, status=status.HTTP_200_OK)


class RawUpdateView(UpdateAPIView):
    queryset = Raw.objects.all()
    serializer_class = RawUpdateSerializer
    lookup_field = 'id'

class FieldUpdateView(UpdateAPIView):
    queryset = Field.objects.all()
    serializer_class = FieldUpdateSerializer
    lookup_field = 'id'


# Fetch estimation details for a specific image
class EstimationView(APIView):
    def get(self, request, image_id):
        estimation = get_object_or_404(ImageHistory, id=image_id, processed=True)
        serializer = HistorySerializer(estimation)
        return Response(serializer.data, status=200)

# Fetch latest completed estimations
class LatestEstimationView(APIView):
    def get(self, request):
        latest_estimations = ImageHistory.objects.filter(processed=True).order_by('-id')[:10]
        if not latest_estimations.exists():
            return Response({"error": "No recent estimations found."}, status=404)

        serializer = HistorySerializer(latest_estimations, many=True)
        return Response({"latest_estimations": serializer.data}, status=200)

# Fetch all history records
class HistoryView(APIView):
    def get(self, request):
        history = ImageHistory.objects.filter(processed=True)
        if not history.exists():
            return Response({"error": "No history records found."}, status=404)

        serializer = HistorySerializer(history, many=True)
        return Response({"history": serializer.data}, status=200)

# Fetch a single historical record
class HistoryDetailView(APIView):
    def get(self, request, history_id):
        history = get_object_or_404(ImageHistory, id=history_id, processed=True)
        serializer = HistorySerializer(history)
        return Response(serializer.data, status=200)


# View to fetch ML results for an image

class MLResultView(APIView):
    """Handles both GET (fetch ML results) and POST (receive ML results)."""

    def get(self, request, image_id):
        """Fetches ML results for a processed image."""
        image = get_object_or_404(ImageHistory, id=image_id, processed=True)

        serializer = MLResultSerializer(image)
        return Response(serializer.data, status=200)

    def post(self, request, image_id):
        """Receives ML processing results from Flask."""
        image = get_object_or_404(ImageHistory, id=image_id)

        # Extract data from ML service
        nb_apples = request.data.get("nb_apples")
        confidence_score = request.data.get("confidence_score")
        processed = request.data.get("processed")

        if nb_apples is None or confidence_score is None or processed is None:
            return Response({"error": "Invalid ML result payload."}, status=400)

        # Update ImageHistory model
        image.nb_apfel = nb_apples
        image.confidence_score = confidence_score
        image.processed = processed
        image.save()

        return Response({"message": "ML result successfully received."}, status=200)


# 1️⃣ Django → ML: Sending Image for Processing
class ProcessImageView(APIView):
    def post(self, request):
        image_id = request.data.get("image_id")
        image = get_object_or_404(ImageHistory, id=image_id)

        payload = {
            "image_url": settings.MEDIA_URL + image.image_path,
            "image_id": image.id,
        }

        try:
            response = requests.post(f"{settings.ML_API_URL}process", json=payload)

            if response.status_code == 200:
                return Response({"message": "Image received, processing started."}, status=status.HTTP_200_OK)
            else:
                return Response({"error": "ML processing failed"}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

        except requests.RequestException as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

# 2️⃣ Retry ML Processing
class RetryProcessingView(APIView):
    def post(self, request):
        image_id = request.data.get("image_id")
        image = get_object_or_404(ImageHistory, id=image_id)

        if image.processed:
            return Response({"error": "Image already processed successfully."}, status=status.HTTP_400_BAD_REQUEST)

        payload = {
            "image_url": settings.MEDIA_URL + image.image_path,
            "image_id": image.id,
        }

        try:
            response = requests.post(f"{settings.ML_API_URL}process", json=payload)

            if response.status_code == 200:
                return Response({"message": "Image processing retry has been requested."}, status=status.HTTP_200_OK)
            else:
                return Response({"error": "Retry failed. ML service issue."}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

        except requests.RequestException as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

# 3️⃣ Fetch ML Model Version
class MLVersionView(APIView):
    def get(self, request):
        try:
            response = requests.get(f"{settings.ML_API_URL}version")

            if response.status_code == 200:
                return Response(response.json(), status=status.HTTP_200_OK)
            else:
                return Response({"error": "ML service unavailable."}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

        except requests.RequestException as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

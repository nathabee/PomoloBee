# views.py

from rest_framework import status, viewsets
from rest_framework.views import APIView
from rest_framework.response import Response
from django.shortcuts import get_object_or_404
from django.core.files.storage import default_storage
from django.conf import settings
import os
import requests  


from .models import Field, Fruit, Raw, ImageHistory
from .serializers import (
    FieldSerializer, FruitSerializer, FieldLocationSerializer,
    ImageSerializer, ImageUploadSerializer, 
    HistorySerializer, MLResultSerializer
)
#RawUpdateSerializer,FieldUpdateSerializer, 


ML_API_URL = os.getenv("ML_API_URL", "http://ml-service:5000/ml")


def api_error(code, message, status_code):
    return Response({
        "error": {
            "code": code,
            "message": message
        }
    }, status=status_code)


def api_success(data, status_code=status.HTTP_200_OK):
    return Response({
        "status": "success",
        "data": data
    }, status=status_code)

def ml_unavailable_with_image(image_id):
    return Response({
        "error": {
            "code": "ML_UNAVAILABLE",
            "message": "ML service unavailable. Image has been saved and can be retried.",
            "image_id": image_id
        }
    }, status=status.HTTP_503_SERVICE_UNAVAILABLE)


class FieldViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Field.objects.all()
    serializer_class = FieldSerializer

    def list(self, request, *args, **kwargs):
        queryset = self.get_queryset()
        serializer = self.get_serializer(queryset, many=True)
        return api_success({"fields": serializer.data}, status_code=status.HTTP_200_OK)

class FruitViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Fruit.objects.all()
    serializer_class = FruitSerializer

    def list(self, request, *args, **kwargs):
        queryset = self.get_queryset()
        serializer = self.get_serializer(queryset, many=True)
        return api_success({"fruits": serializer.data}, status_code=status.HTTP_200_OK)


class LocationListView(APIView):
    def get(self, request):
        fields = Field.objects.prefetch_related('raws__fruit').all()
        if not fields.exists():
            return api_error("NO_DATA", "No field and raw data available.", status.HTTP_404_NOT_FOUND)
        serializer = FieldLocationSerializer(fields, many=True)
        return api_success({"locations": serializer.data})


class ImageListView(APIView):
    def get(self, request):
        images = ImageHistory.objects.all()
        serializer = ImageSerializer(images, many=True)
        return api_success({"images": serializer.data})


class ImageDetailView(APIView):
    def get(self, request, image_id):
        image = get_object_or_404(ImageHistory, id=image_id)
        serializer = ImageSerializer(image)
        return api_success(serializer.data)


class ImageStatusView(APIView):
    def get(self, request, image_id):
        image = get_object_or_404(ImageHistory, id=image_id)
        return api_success({
            "image_id": image.id,
            "status": "done" if image.processed else "processing",
            "processed": image.processed,
            "nb_apfel": image.nb_apfel,
            "confidence_score": image.confidence_score
        })


class ErrorLogView(APIView):
    def get(self, request, image_id):
        _ = get_object_or_404(ImageHistory, id=image_id)
        return api_success({"error_log": "No errors found."})


class ImageDeleteView(APIView):
    def delete(self, request, image_id):
        image = get_object_or_404(ImageHistory, id=image_id)
        default_storage.delete(image.image_path)
        image.delete()
        return api_success({"message": "Image deleted successfully."})


class EstimationView(APIView):
    def get(self, request, image_id):
        estimation = get_object_or_404(ImageHistory, id=image_id, processed=True)
        serializer = HistorySerializer(estimation)
        return api_success(serializer.data)


class LatestEstimationView(APIView):
    def get(self, request):
        latest_estimations = ImageHistory.objects.filter(processed=True).order_by('-id')[:10]
        if not latest_estimations.exists():
            return api_error("NO_DATA", "No recent estimations found.", status.HTTP_404_NOT_FOUND)
        serializer = HistorySerializer(latest_estimations, many=True)
        return api_success({"latest_estimations": serializer.data})


class HistoryView(APIView):
    def get(self, request):
        history = ImageHistory.objects.filter(processed=True)
        if not history.exists():
            return api_error("NO_DATA", "No history records found.", status.HTTP_404_NOT_FOUND)
        serializer = HistorySerializer(history, many=True)
        return api_success({"history": serializer.data})


class HistoryDetailView(APIView):
    def get(self, request, history_id):
        history = get_object_or_404(ImageHistory, id=history_id, processed=True)
        serializer = HistorySerializer(history)
        return api_success(serializer.data)


class MLResultView(APIView):
    def get(self, request, image_id):
        image = get_object_or_404(ImageHistory, id=image_id, processed=True)
        serializer = MLResultSerializer(image)
        return api_success(serializer.data)

    def post(self, request, image_id):
        """Receives ML processing results from Flask."""
        image = get_object_or_404(ImageHistory, id=image_id)

        nb_apples = request.data.get("nb_apples")
        confidence_score = request.data.get("confidence_score")
        processed = request.data.get("processed")

        if nb_apples is None or confidence_score is None or processed is None:
            return api_error("MISSING_PARAMETER", "Missing required fields in ML payload.", status.HTTP_400_BAD_REQUEST)

        image.nb_apfel = nb_apples
        image.confidence_score = confidence_score
        image.processed = processed
        image.save()

        return api_success({"message": "ML result successfully received."})


class ImageView(APIView):
    def get(self, request):
        images = ImageHistory.objects.all()
        serializer = ImageSerializer(images, many=True)
        return api_success({"images": serializer.data})

    def post(self, request):
        serializer = ImageUploadSerializer(data=request.data)
        if serializer.is_valid():
            image_file = serializer.validated_data['image']
            raw_id = serializer.validated_data['raw_id']
            date = serializer.validated_data['date']
            file_path = default_storage.save(f'images/{image_file.name}', image_file)

            image_history = ImageHistory.objects.create(
                image_path=file_path,
                raw_id=raw_id,
                date=date,
                processed=False
            )

            payload = {
                "image_url": settings.MEDIA_URL + file_path,
                "image_id": image_history.id
            }

            try:
                response = requests.post(f"{ML_API_URL}/process-image", json=payload, timeout=5)
                if response.status_code == 200:
                    return api_success({
                        "image_id": image_history.id,
                        "message": "Image uploaded successfully and queued for processing."
                    }, status.HTTP_201_CREATED)
                else:
                    return ml_unavailable_with_image(image_history.id)

            except requests.RequestException as e:
                return api_error("ML_UNAVAILABLE", f"Image uploaded but ML call failed: {str(e)}", status.HTTP_503_SERVICE_UNAVAILABLE)

        return api_error("INVALID_INPUT", serializer.errors, status.HTTP_400_BAD_REQUEST)




class RetryProcessingView(APIView):
    def post(self, request):
        image_id = request.data.get("image_id")
        image = get_object_or_404(ImageHistory, id=image_id)

        if image.processed:
            return api_error("ALREADY_PROCESSED", "Image already processed successfully.", status.HTTP_409_CONFLICT)

        payload = {
            "image_url": settings.MEDIA_URL + image.image_path,
            "image_id": image.id,
        }

        try:
            response = requests.post(f"{ML_API_URL}/process-image", json=payload, timeout=5)
            if response.status_code == 200:
                return api_success({"message": "Image processing retry has been requested."})
            else:
                return api_error("ML_RETRY_FAILED", "Retry failed. ML service issue. status code {response.status_code}", status.HTTP_500_INTERNAL_SERVER_ERROR)
        except requests.RequestException as e:
            return api_error("ML_UNAVAILABLE", f"ML retry failed: {str(e)}", status.HTTP_503_SERVICE_UNAVAILABLE)



class MLVersionView(APIView):
    def get(self, request):
        try:
            response = requests.get(f"{ML_API_URL}/version", timeout=5)
            if response.status_code == 200:
                ml_data = response.json()
                return api_success(ml_data.get("data", {}))
            else:
                return api_error(
                    "ML_UNAVAILABLE",
                    f"ML service unavailable: Timeout or status code {response.status_code}",
                    status.HTTP_503_SERVICE_UNAVAILABLE
                )

        except requests.RequestException as e:
            return api_error(
                "ML_UNAVAILABLE",
                f"ML service unavailable: {str(e)}",
                status.HTTP_503_SERVICE_UNAVAILABLE
            )

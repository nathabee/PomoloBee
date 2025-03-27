from rest_framework import status
from django.core.files.storage import default_storage
from django.conf import settings
from django.utils import timezone
import requests
import logging

from .exceptions import APIError, MLUnavailableError
from core.utils import get_object_or_error
from .models import Field, Fruit, Image, Estimation
from .serializers import (
    FieldSerializer, FruitSerializer, FieldLocationSerializer,
    ImageSerializer, ImageUploadSerializer, 
    EstimationSerializer, MLResultSerializer
)
from .utils import BaseAPIView, BaseReadOnlyViewSet

logger = logging.getLogger(__name__)
ML_API_URL = settings.ML_API_URL


# ---------- FIELD + FRUIT ----------
class FieldViewSet(BaseReadOnlyViewSet):
    queryset = Field.objects.all()
    serializer_class = FieldSerializer


class FruitViewSet(BaseReadOnlyViewSet):
    queryset = Fruit.objects.all()
    serializer_class = FruitSerializer


# ---------- LOCATION ----------
class LocationListView(BaseAPIView):
    def get(self, request):
        fields = Field.objects.prefetch_related('raws__fruit').all()
        if not fields.exists():
            raise APIError("NO_DATA", "No field and raw data available.", status.HTTP_404_NOT_FOUND)
        serializer = FieldLocationSerializer(fields, many=True)
        return self.success({"locations": serializer.data})


# ---------- IMAGE ---------- 


class ImageDetailView(BaseAPIView): 
    def get(self, request, image_id):
        image = get_object_or_error(Image, id=image_id)
        serializer = ImageSerializer(image, context={'request': request})
        return self.success(serializer.data)


 

class ImageDeleteView(BaseAPIView):
    def delete(self, request, image_id):
        image = get_object_or_error(Image, id=image_id)
        if image.image_file:
            default_storage.delete(image.image_file.path)
        image.delete()
        return self.success({"message": "Image deleted successfully."})


class ImageView(BaseAPIView):
    def post(self, request):
        serializer = ImageUploadSerializer(data=request.data)
        if serializer.is_valid():
            image_file = serializer.validated_data['image']
            raw_id = serializer.validated_data['raw_id']
            date = serializer.validated_data['date']
            file_path = default_storage.save(f'images/{image_file.name}', image_file)

            image = Image.objects.create(
                image_file=file_path,
                raw_id=raw_id,
                date=date,
                processed=False
            )

            payload = {
                "image_url": settings.MEDIA_URL + image.image_file.name,
                "image_id": image.id
            }

            try:
                response = requests.post(f"{ML_API_URL}/process-image", json=payload, timeout=5)
                if response.status_code == 200:
                    return self.success({
                        "image_id": image.id,
                        "message": "Image uploaded successfully and queued for processing."
                    }, status.HTTP_201_CREATED)
                else:
                    raise MLUnavailableError(detail="ML call failed", image_id=image.id)

            except requests.RequestException as e:
                raise MLUnavailableError(detail=str(e), image_id=image.id)

        logger.warning("ImageUploadSerializer failed: %s", serializer.errors)
        raise APIError("INVALID_INPUT", serializer.errors, status.HTTP_400_BAD_REQUEST)


# ---------- ESTIMATION ----------
class FieldEstimationListView(BaseAPIView):
    def get(self, request, field_id):
        estimations = Estimation.objects.filter(raw__field_id=field_id).order_by('-timestamp')
        if not estimations.exists():
            raise APIError("404_NOT_FOUND", "No estimation found.", status.HTTP_404_NOT_FOUND)
        serializer = EstimationSerializer(estimations, many=True)
        return self.success({"estimations": serializer.data})

 

class EstimationView(BaseAPIView):
    def get(self, request, image_id):
        estimation = Estimation.objects.filter(image_id=image_id).first()
        if not estimation:
            raise APIError("404_NOT_FOUND", "Estimation not found.", status.HTTP_404_NOT_FOUND)
        serializer = EstimationSerializer(estimation)
        return self.success( serializer.data)

 

# ---------- ML RESULT ----------
class MLResultView(BaseAPIView):
    def get(self, request, image_id):
        image = get_object_or_error(Image, id=image_id, processed=True)
        serializer = MLResultSerializer(image)
        return self.success(serializer.data)

    def post(self, request, image_id):
        image = get_object_or_error(Image, id=image_id)

        nb_fruit = request.data.get("nb_fruit")
        confidence_score = request.data.get("confidence_score")
        processed = request.data.get("processed")

        if nb_fruit is None or confidence_score is None or processed is None:
            raise APIError("MISSING_PARAMETER", "Missing required fields in ML payload.", status.HTTP_400_BAD_REQUEST)

        image.nb_fruit = nb_fruit
        image.confidence_score = confidence_score
        image.processed = processed
        image.processed_at = timezone.now()
        image.save()

        logger.info(f"Triggering yield estimation for Image {image.id}")

        if not Estimation.objects.filter(image=image).exists():
            raw = image.raw
            fruit = raw.fruit
            plant_kg = nb_fruit * fruit.fruit_avg_kg
            raw_kg = plant_kg * raw.nb_plant

            Estimation.objects.create(
                image=image,
                date=image.date or timezone.now().date(),
                raw=raw,
                plant_fruit=nb_fruit,
                plant_kg=plant_kg,
                raw_kg=raw_kg,
                estimated_yield_kg=raw_kg,
                confidence_score=confidence_score or 0,
                source='MLI'
            )

        return self.success({"message": "ML result successfully received."})


class RetryProcessingView(BaseAPIView):
    def post(self, request):
        image_id = request.data.get("image_id")
        image = get_object_or_error(Image, id=image_id)

        if image.processed:
            raise APIError("ALREADY_PROCESSED", "Image already processed successfully.", status.HTTP_409_CONFLICT)

        payload = {
            "image_url": settings.MEDIA_URL + image.image_file.name,
            "image_id": image.id,
        }

        try:
            response = requests.post(f"{ML_API_URL}/process-image", json=payload, timeout=5)
            if response.status_code == 200:
                return self.success({"message": "Image processing retry has been requested."})
            else:
                raise APIError("ML_RETRY_FAILED", "Retry failed. ML service issue.", status.HTTP_500_INTERNAL_SERVER_ERROR)
        except requests.RequestException as e:
            raise MLUnavailableError(detail=f"ML retry failed: {str(e)}", image_id=image.id)


# ---------- ML VERSION ----------
class MLVersionView(BaseAPIView):
    def get(self, request):
        try:
            response = requests.get(f"{ML_API_URL}/version", timeout=5)
            if response.status_code == 200:
                return self.success(response.json().get("data", {}))
            else:
                raise MLUnavailableError(detail="ML service returned error")
        except requests.RequestException as e:
            raise MLUnavailableError(detail=f"ML service unavailable: {str(e)}")

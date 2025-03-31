import os
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
        fields = Field.objects.prefetch_related('rows__fruit').all()
        if not fields.exists():
            raise APIError("NO_DATA", "No field and row data available.", status.HTTP_404_NOT_FOUND)
        serializer = FieldLocationSerializer(fields, many=True, context={'request': request})
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
        warning = None

        try:
            if image.image_file and default_storage.exists(image.image_file.name):
                default_storage.delete(image.image_file.name)
        except Exception as e:
            warning = f"Could not delete file: {e}"
            print(f"Warning: {warning}")

        image.delete()

        response_data = {"message": "Image deleted successfully."}
        if warning:
            response_data["warning"] = warning

        return self.success(response_data)




class ImageView(BaseAPIView):
    def post(self, request):
        serializer = ImageUploadSerializer(data=request.data)
        if serializer.is_valid():
            image_file = serializer.validated_data['image']
            row_id = serializer.validated_data['row_id']
            date = serializer.validated_data['date']
            # Save original name (optional, for dedup or trace)
            original_filename = image_file.name

            existing = Image.objects.filter(
                original_filename=original_filename,
                row_id=row_id,
                date=date
            ).first()

            if existing:
                return self.success({"image_id": existing.id, "message": "Already uploaded."})




            # Save temp with any name first
            file_path = default_storage.save(f'images/temp_{original_filename}', image_file)

            # Create the Image record
            image = Image.objects.create(
                image_file=file_path,
                row_id=row_id,
                date=date,
                upload_date=timezone.now().date(),
                processed=False,
                status="processing",
                original_filename=original_filename
            )

            # Compute new desired name: image-{id}.jpg
            ext = os.path.splitext(original_filename)[1]  # e.g., .jpg
            new_filename = f"images/image-{image.id}{ext}"
            new_full_path = os.path.join(settings.MEDIA_ROOT, new_filename)

            # Rename the file on disk
            os.rename(os.path.join(settings.MEDIA_ROOT, file_path), new_full_path)

            # Update the model to point to the new name
            image.image_file.name = new_filename 
            image.save()

            image_url = image.image_file.url 
            payload = {
                "image_url": image_url,
                "image_id": image.id
            }

            try:
                response = requests.post(f"{settings.ML_API_URL}/process-image", json=payload, timeout=5)
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


class RetryProcessingView(BaseAPIView):
    def post(self, request):
        image_id = request.data.get("image_id")
        image = get_object_or_error(Image, id=image_id)

        if image.processed:
            raise APIError("ALREADY_PROCESSED", "Image already processed successfully.", status.HTTP_409_CONFLICT)

        image_url = image.image_file.url 
        payload = {
            "image_url": image_url,
            "image_id": image.id
        }


        try:
            response = requests.post(f"{settings.ML_API_URL}/process-image", json=payload, timeout=5)
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
            response = requests.get(f"{settings.ML_API_URL}/version", timeout=5)
            if response.status_code == 200:
                return self.success(response.json().get("data", {}))
            else:
                raise MLUnavailableError(detail="ML service returned error")
        except requests.RequestException as e:
            raise MLUnavailableError(detail=f"ML service unavailable: {str(e)}")



# ---------- ESTIMATION ----------
class FieldEstimationListView(BaseAPIView):
    def get(self, request, field_id):
        estimations = Estimation.objects.filter(row__field_id=field_id).order_by('-timestamp')
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
        image.status = "done" if processed else "failed"  
        image.save()

        logger.info(f"Triggering yield estimation for Image {image.id}")

        if not Estimation.objects.filter(image=image).exists():
            row = image.row
            fruit = row.fruit
            plant_kg = nb_fruit * fruit.fruit_avg_kg
            row_kg = plant_kg * row.nb_plant

            Estimation.objects.create(
                image=image,
                date=image.date or timezone.now().date(),
                row=row,
                plant_fruit=nb_fruit,
                plant_kg=plant_kg,
                row_kg=row_kg,
                estimated_yield_kg=row_kg,
                confidence_score=confidence_score or 0,
                source='MLI'
            )

        return self.success({"message": "ML result successfully received."})


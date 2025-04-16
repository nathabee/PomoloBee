import os
from rest_framework import status
from django.core.files.storage import default_storage
from django.conf import settings
from django.utils import timezone
import requests
import logging

from .exceptions import APIError, MLUnavailableError
from core.utils import get_object_or_error
from .models import Field, Fruit, Image, Estimation, Row
from .serializers import (
    FieldSerializer, FruitSerializer, FieldLocationSerializer,
    ImageSerializer, ImageUploadSerializer, 
    EstimationSerializer,  
)
from .utils import BaseAPIView, BaseReadOnlyViewSet
from rest_framework.parsers import MultiPartParser

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


class ImageListView(BaseAPIView):
    def get(self, request):
        # Optional filters
        field_id = request.GET.get("field_id")
        row_id = request.GET.get("row_id")
        date = request.GET.get("date")

        queryset = Image.objects.all().order_by('-upload_date')

        if field_id:
            queryset = queryset.filter(row__field_id=field_id)
        if row_id:
            queryset = queryset.filter(row_id=row_id)
        if date:
            queryset = queryset.filter(date=date)

        # Pagination (limit/offset)
        limit = int(request.GET.get("limit", 100))
        offset = int(request.GET.get("offset", 0))
        total = queryset.count()
        queryset = queryset[offset:offset+limit]

        serializer = ImageSerializer(queryset, many=True, context={"request": request})
        return self.success({
            "total": total,
            "limit": limit,
            "offset": offset,
            "images": serializer.data
        })



class ImageView(BaseAPIView):
    def post(self, request):
        serializer = ImageUploadSerializer(data=request.data)
        if serializer.is_valid():
            image_file = serializer.validated_data['image']
            row_id = serializer.validated_data['row_id']
            date = serializer.validated_data['date']
            # Save original name (optional, for dedup or trace)
            user_fruit_plant  = serializer.validated_data.get('user_fruit_plant', None)
            original_filename = image_file.name
            xy_location = serializer.validated_data.get('xy_location', None)


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
                xy_location=xy_location,
                user_fruit_plant=user_fruit_plant,
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

    
 
class MLResultView(BaseAPIView):  
    def post(self, request, image_id):
        logger.debug(f"🔍 Incoming ML result POST for image_id={image_id}")
        logger.debug(f"📦 Raw request data: {request.data}")

        try:
            image = get_object_or_error(Image, id=image_id)
        except APIError as e:
            logger.error(f"❌ Image not found for ML result (image_id={image_id}): {str(e)}")
            raise

        
        try:
            fruit_plant = float(request.data.get("fruit_plant"))
        except (TypeError, ValueError):
            raise APIError("INVALID_PARAMETER", "fruit_plant must be a numeric value.", status.HTTP_400_BAD_REQUEST)
        
        confidence_score = request.data.get("confidence_score")
        processed = request.data.get("processed")

        # Convert processed from string to boolean if needed
        if isinstance(processed, str):
            processed = processed.lower() in ["true", "1", "yes"]

        logger.debug(f"🧮 Parsed ML data: fruit_plant={fruit_plant}, confidence_score={confidence_score}, processed={processed}")

        if fruit_plant is None or confidence_score is None or processed is None:
            logger.warning(f"⚠️ Missing parameters in ML payload: {request.data}")
            raise APIError("MISSING_PARAMETER", "Required: fruit_plant, confidence_score, processed", status.HTTP_400_BAD_REQUEST)

        image.processed = processed
        image.processed_at = timezone.now()
        image.status = "done" if processed else "failed"  
        image.save()

        logger.info(f"✅ Updated image {image_id} with processed={processed}")

        if not Estimation.objects.filter(image=image).exists():
            row = image.row

            logger.info(f"📈 Creating new estimation for image {image.id}: fruit_plant : {fruit_plant}")

            Estimation.objects.create(
                image=image,
                date=image.date or timezone.now().date(),
                row=row,
                fruit_plant=fruit_plant,
                confidence_score=confidence_score or 0,
                source='MLI'
            )
        else:
            logger.info(f"🟡 Estimation already exists for image {image.id}")

        return self.success({"message": "ML result successfully received."})



class ManualEstimationView(BaseAPIView):
    parser_classes = [MultiPartParser]  # Allow file upload (optional)

    def post(self, request):
        data = request.data

        row_id = data.get("row_id")
        date = data.get("date")
        xy_location = data.get("xy_location")
        try:
            fruit_plant = float(request.data.get("fruit_plant"))
        except (TypeError, ValueError):
            raise APIError("INVALID_PARAMETER", "fruit_plant must be a numeric value.", status.HTTP_400_BAD_REQUEST)

        confidence_score = data.get("confidence_score", None)
        maturation_grade = data.get("maturation_grade", 0.0)
        image_file = request.FILES.get("image", None)

        # Validation
        if not all([row_id, date, fruit_plant]):
            return self.error("MISSING_FIELDS", "Required fields: row_id, date, fruit_plant")

        row = get_object_or_error(Row, id=row_id)
        fruit = row.fruit

        # 1. Handle image: uploaded or default
        if image_file:
            original_filename = image_file.name
            temp_path = default_storage.save(f'images/temp_manual_{original_filename}', image_file)
            ext = os.path.splitext(original_filename)[1]
            image = Image.objects.create(
                row=row,
                date=date,
                upload_date=timezone.now().date(),
                processed=True,
                processed_at = timezone.now(),
                user_fruit_plant=fruit_plant,
                status="manual",
                original_filename=None,
                xy_location=xy_location
            )
            final_path = f"images/image-{image.id}{ext}"
            os.rename(os.path.join(settings.MEDIA_ROOT, temp_path), os.path.join(settings.MEDIA_ROOT, final_path))
            image.image_file.name = final_path
            image.save()
        else:
            # Use default placeholder
            image = Image.objects.create(
                row=row,
                date=date,
                upload_date=timezone.now().date(),
                processed=True,
                processed_at = timezone.now(),
                user_fruit_plant=fruit_plant,
                status="manual",
                original_filename=None,
                image_file="images/image_default.jpg",  # This must exist in media/
                xy_location=xy_location
            )

        # 2. Create estimation calculated in modele after save 
        #plant_kg = float(fruit_plant) * fruit.fruit_avg_kg
        #row_kg =  plant_kg * float(row.nb_plant)

        estimation = Estimation.objects.create(
            image=image,
            row=row,
            date=date,
            fruit_plant=fruit_plant,
            confidence_score=confidence_score,  
            maturation_grade=maturation_grade,
            source=Estimation.EstimationSource.USER
        )

        serializer = EstimationSerializer(estimation, context={'request': request})
        return self.success({
            "image_id": image.id,
            "estimation_id": estimation.id,
            "estimations": serializer.data
        }, status_code=201)
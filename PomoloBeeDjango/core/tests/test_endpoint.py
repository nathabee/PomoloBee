#  test: URL endpoint responses

from django.test import TestCase
from django.urls import reverse 
from rest_framework import status 
from django.contrib.auth.models import User
from core.models import Field, Fruit, Raw, Farm
 
 

# will be called with
# python manage.py test core

  


class GetEndpointsTest(TestCase):
    """Test GET /fields/, GET /fruits/, and GET /locations/ API endpoints."""

    def setUp(self):
        """Set up test data for fields, fruits, and locations."""
        # Create dummy user + farm (required by Field.id_farm FK)
        self.user = User.objects.create_user(username='testuser', password='testpass')
        self.farm = Farm.objects.create(name="Test Farm", owner=self.user)

        self.field1 = Field.objects.create(
            short_name="North_Field", name="North Orchard",
            description="Main apple orchard", orientation="N",
            id_farm=self.farm
        )
        self.field2 = Field.objects.create(
            short_name="South_Field", name="South Orchard",
            description="Mixed fruit orchard", orientation="S",
            id_farm=self.farm
        )

        self.fruit1 = Fruit.objects.create(
            short_name="Golden_Apple", name="Golden Apple",
            description="Sweet yellow apples",
            yield_start_date="2024-09-01", yield_end_date="2024-10-15",
            yield_avg_kg=2.5, fruit_avg_kg=0.3
        )
        self.fruit2 = Fruit.objects.create(
            short_name="Red_Apple", name="Red Apple",
            description="Crunchy red apples",
            yield_start_date="2024-07-15", yield_end_date="2024-08-30",
            yield_avg_kg=2.8, fruit_avg_kg=0.35
        )

        self.raw1 = Raw.objects.create(
            short_name="Row_A", name="Row A", nb_plant=50,
            fruit=self.fruit1, field=self.field1
        )
        self.raw2 = Raw.objects.create(
            short_name="Row_B", name="Row B", nb_plant=40,
            fruit=self.fruit2, field=self.field1
        )

    def test_get_fields(self):
        response = self.client.get(reverse('fields-list'))
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("fields", response.json()["data"])
        self.assertEqual(len(response.json()["data"]["fields"]), 2)
        self.assertEqual(response.json()["data"]["fields"][0]["name"], "North Orchard")

    def test_get_fruits(self):
        response = self.client.get(reverse('fruits-list'))
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("fruits", response.json()["data"])
        self.assertEqual(len(response.json()["data"]["fruits"]), 2)
        self.assertEqual(response.json()["data"]["fruits"][0]["name"], "Golden Apple")


    def test_get_locations(self):
        response = self.client.get(reverse('locations'))
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("locations", response.json()["data"])
        self.assertEqual(len(response.json()["data"]["locations"]), 2)
        self.assertEqual(len(response.json()["data"]["locations"][0]["raws"]), 2)


     # Uploading Images
 
    #POST /api/images/ → Upload image + start ML
    def test_post_image_upload(self):
        from io import BytesIO
        from PIL import Image
        from django.core.files.uploadedfile import SimpleUploadedFile

        # Create dummy image in memory
        image_file = BytesIO()
        Image.new("RGB", (100, 100)).save(image_file, format="JPEG")
        image_file.seek(0)

        uploaded_file = SimpleUploadedFile("test.jpg", image_file.read(), content_type="image/jpeg")

        response = self.client.post(
            reverse("images"),  # Name for /api/images/
            {
                "image": uploaded_file,
                "raw_id": self.raw1.id,
                "date": "2024-03-14"
            }
        )

        self.assertEqual(response.status_code, 201)
        data = response.json()
        self.assertIn("data", data)
        self.assertIn("image_id", data["data"])




    # Fetching Estimation Results 
    # GET /api/estimations/{image_id}/
    def test_get_estimation_results(self):
        from core.models import ImageHistory, HistoryRaw, HistoryEstimation

        # First create a processed image
        img = ImageHistory.objects.create(
            image_path="test.jpg",
            raw=self.raw1,
            date="2024-03-14",
            processed=True,
            nb_apfel=10,
            confidence_score=0.9
        )

        # Simulate that signal has created history
        HistoryRaw.objects.create(
            id_image=img, raw=self.raw1, date="2024-03-14",
            plant_apfel=10, plant_kg=3.0, raw_kg=150.0
        )
        HistoryEstimation.objects.create(
            raw=self.raw1, date="2024-03-14", estimated_yield_kg=150.0, confidence_score=0.9
        )

        response = self.client.get(reverse("estimation-detail", args=[img.id]))
        self.assertEqual(response.status_code, 200)
        self.assertIn("plant_kg", response.json()["data"])


    # Checking Processing Status
    # GET /api/images/{image_id}/status/
    def test_get_image_status(self):
        from core.models import ImageHistory
        img = ImageHistory.objects.create(image_path="demo.jpg", raw=self.raw1, processed=True)
        response = self.client.get(reverse("image-status", args=[img.id]))
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["data"]["processed"], True)



    # Fetching Image List
    # GET /api/images/        
    def test_get_image_list(self):
        response = self.client.get(reverse("images"))
        self.assertEqual(response.status_code, 200)
        self.assertIn("images", response.json()["data"])


    # Fetching Image Details 
    # GET /api/images/{image_id}/details/
    def test_get_image_details(self):
        from core.models import ImageHistory
        img = ImageHistory.objects.create(image_path="demo.jpg", raw=self.raw1)
        response = self.client.get(reverse("image-detail", args=[img.id]))
        self.assertEqual(response.status_code, 200)


    # Fetching Processing Errors
    # GET /api/images/{image_id}/error_log
    def test_get_error_log(self):
        from core.models import ImageHistory
        img = ImageHistory.objects.create(image_path="demo.jpg", raw=self.raw1)
        response = self.client.get(reverse("image-error-log", args=[img.id]))
        self.assertEqual(response.status_code, 200)

    # GET /api/latest_estimations/
    def test_get_latest_estimations(self):
        response = self.client.get(reverse("latest-estimations"))
        self.assertEqual(response.status_code, 200)


    # Fetching History of Estimations
        # GET /api/history/
    def test_get_history(self):
        response = self.client.get(reverse("history-list"))
        self.assertIn(response.status_code, [200, 404])  # Allow empty case


        # GET /api/history/{history_id}/
    def test_get_history_detail(self):
        from core.models import ImageHistory, HistoryRaw
        img = ImageHistory.objects.create(
            image_path="test.jpg", raw=self.raw1, date="2024-03-14", processed=True
        )
        hr = HistoryRaw.objects.create(
            id_image=img, raw=self.raw1, date="2024-03-14",
            plant_apfel=10, plant_kg=3.0, raw_kg=150.0
        )
        response = self.client.get(reverse("history-detail", args=[hr.id]))
        self.assertEqual(response.status_code, 200)
 

    # Deleting an Image 
    # DELETE /api/images/{image_id}/
    def test_delete_image(self):
        from core.models import ImageHistory
        image = ImageHistory.objects.create(
            image_path="fake_path.jpg", raw=self.raw1, date="2024-03-14"
        )
        response = self.client.delete(reverse("image-delete", args=[image.id]))
        self.assertEqual(response.status_code, 200)
        self.assertFalse(ImageHistory.objects.filter(id=image.id).exists())


    # ML → Django: Posting ML result 
    # POST /api/images/{image_id}/ml_result
    def test_post_ml_result_to_django(self):
        from core.models import ImageHistory

        image = ImageHistory.objects.create(
            image_path="result_test.jpg",
            raw=self.raw1,
            date="2024-03-14",
            processed=False
        )

        response = self.client.post(
            reverse("ml-result", args=[image.id]),
            {
                "nb_apples": 12,
                "confidence_score": 0.89,
                "processed": True
            },
            content_type="application/json"
        )

        self.assertEqual(response.status_code, 200)
        image.refresh_from_db()
        self.assertTrue(image.processed)
        self.assertEqual(image.nb_apfel, 12)
        self.assertAlmostEqual(image.confidence_score, 0.89, places=2)

    # Request Retry for ML Processing 
        # POST /api/retry_processing/
    def test_post_retry_processing(self):
        from core.models import ImageHistory

        img = ImageHistory.objects.create(
            image_path="test.jpg",
            raw=self.raw1,
            date="2024-03-14",
            processed=False
        )

        response = self.client.post(
            reverse("retry-processing"),
            {"image_id": img.id},
            content_type="application/json"
        )

        self.assertIn(response.status_code, [200, 503])  # Allow offline ML test


    # App fetches ML results (Django-side) 
        # GET /api/images/{image_id}/ml_result
    def test_post_ml_result_to_django(self):
        from core.models import ImageHistory

        image = ImageHistory.objects.create(
            image_path="fake.jpg",
            raw=self.raw1,
            date="2024-03-14",
            processed=False
        )

        response = self.client.post(
            reverse("ml-result", args=[image.id]),
            {
                "nb_apples": 12,
                "confidence_score": 0.85,
                "processed": True
            },
            content_type="application/json"
        )

        self.assertEqual(response.status_code, 200)
        image.refresh_from_db()
        self.assertTrue(image.processed)
        self.assertEqual(image.nb_apfel, 12)


    
    def test_get_ml_version(self):
        response = self.client.get(reverse('ml-version'))  # Make sure your URL name is correct
        self.assertEqual(response.status_code, 200)

        data = response.json()
        
        # Check standard response structure
        self.assertIn("status", data)
        self.assertEqual(data["status"], "success")
        self.assertIn("data", data)

        ml_data = data["data"]
        
        # Check required fields are present
        self.assertIn("model_version", ml_data)
        self.assertIn("status", ml_data)
        self.assertIn("last_updated", ml_data)

        # Check field types / values
        self.assertIsInstance(ml_data["model_version"], str)
        self.assertIsInstance(ml_data["status"], str)
        self.assertRegex(ml_data["last_updated"], r"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$")  # ISO 8601 datetime



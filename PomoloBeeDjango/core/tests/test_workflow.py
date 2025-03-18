# Validation test: Full API workflow
from django.test import TestCase
from django.core.files.uploadedfile import SimpleUploadedFile
from django.urls import reverse
from django.conf import settings
import requests
from rest_framework import status
from core.models import ImageHistory, Field, Fruit, Raw

class DjangoWorkflowTest(TestCase):
    """Test full Django workflow including database initialization, API calls, and ML processing."""

    fixtures = ["initial_fields.json", "initial_fruits.json", "initial_raws.json"]

    def setUp(self):
        """Set up URLs for API calls."""
        self.upload_url = reverse("image-upload")
        self.ml_result_url = reverse("ml-result", args=[1])  # Assuming ML result for image_id=1
        self.process_url = f"{settings.ML_API_URL}/process"

    ### 1️⃣ TEST INITIAL DATABASE STATE ###
    def test_01_check_fixture_loading(self):
        """Check if fixtures were correctly loaded."""
        self.assertEqual(Field.objects.count(), 6, "Expected 6 fields in the database.")
        self.assertEqual(Fruit.objects.count(), 6, "Expected 6 fruits in the database.")
        self.assertEqual(Raw.objects.count(), 28, "Expected 28 raws in the database.")

    def test_02_check_example_data(self):
        """Check that specific raw, fruit, and field exist with correct values."""
        field = Field.objects.get(short_name="C1")
        self.assertEqual(field.name, "ChampMaison")

        fruit = Fruit.objects.get(short_name="Swing_CG1")
        self.assertEqual(fruit.name, "Cultivar Swing on CG1")

        raw = Raw.objects.get(short_name="C1-R3")
        self.assertEqual(raw.name, "Rang 3 cote maison Swing 3")
        self.assertEqual(raw.nb_plant, 40)
        self.assertEqual(raw.field.id, 1)  # Foreign key to Field
        self.assertEqual(raw.fruit.id, 1)  # Foreign key to Fruit

    ### 2️⃣ TEST INITIAL API ENDPOINTS ###
    def test_03_get_fields(self):
        """Test GET /api/fields/ returns the correct data."""
        response = self.client.get(reverse("fields-list"))
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.json()), 6)  # Expecting 6 fields
        self.assertEqual(response.json()[0]["short_name"], "C1")

    def test_04_get_fruits(self):
        """Test GET /api/fruits/ returns the correct data."""
        response = self.client.get(reverse("fruits-list"))
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.json()), 6)  # Expecting 6 fruits
        self.assertEqual(response.json()[0]["short_name"], "Swing_CG1")

    def test_05_get_locations(self):
        """Test GET /api/locations/ returns fields + associated raws."""
        response = self.client.get(reverse("locations"))
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("locations", response.json())
        self.assertEqual(len(response.json()["locations"]), 6)  # Expecting 6 fields
        self.assertEqual(len(response.json()["locations"][0]["raws"]), 2)  # Example: Field 1 has 2 raws

    ### 3️⃣ TEST IMAGE UPLOAD & PROCESSING ###
    def test_06_upload_image(self):
        """Test image upload to Django."""
        image = SimpleUploadedFile("orchard.jpg", b"file_content", content_type="image/jpeg")
        response = self.client.post(self.upload_url, {"image": image, "raw_id": 1, "date": "2024-03-14"}, format="multipart")

        self.assertEqual(response.status_code, 201)
        self.assertIn("image_id", response.json())

    def test_07_django_sends_image_to_ml(self):
        """Test if Django correctly sends an image processing request to ML."""
        image = ImageHistory.objects.create(image_path="test_image.jpg", processed=False)

        payload = {"image_url": f"{settings.MEDIA_URL}{image.image_path}", "image_id": image.id}
        response = requests.post(self.process_url, json=payload)

        self.assertEqual(response.status_code, 200)
        self.assertIn("message", response.json())

    def test_08_ml_sends_results_back_to_django(self):
        """Test if ML successfully sends processing results back to Django."""
        image = ImageHistory.objects.create(image_path="test_image.jpg", processed=False)

        payload = {"nb_apples": 15, "confidence_score": 0.85, "processed": True}
        response = self.client.post(reverse("ml-result", args=[image.id]), data=payload, content_type="application/json")

        self.assertEqual(response.status_code, 200)
        self.assertIn("message", response.json())

        # Check if ImageHistory is updated
        image.refresh_from_db()
        self.assertEqual(image.nb_apfel, 15)
        self.assertEqual(image.confidence_score, 0.85)
        self.assertTrue(image.processed)

    def test_09_django_fetches_ml_results(self):
        """Test if Django retrieves ML results."""
        image = ImageHistory.objects.create(image_path="test_image.jpg", nb_apfel=10, confidence_score=0.9, processed=True)

        response = self.client.get(reverse("ml-result", args=[image.id]))

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["nb_apples"], 10)
        self.assertEqual(response.json()["confidence_score"], 0.9)

    def test_10_fetch_estimations(self):
        """Test if Django correctly returns yield estimations."""
        response = self.client.get(reverse("estimation-detail", args=[1]))  # Assuming image_id=1 exists

        self.assertEqual(response.status_code, 200)
        self.assertIn("plant_apfel", response.json())

    def test_11_fetch_estimation_history(self):
        """Test if Django correctly fetches history."""
        response = self.client.get(reverse("history-list"))

        self.assertEqual(response.status_code, 200)
        self.assertIn("history", response.json())

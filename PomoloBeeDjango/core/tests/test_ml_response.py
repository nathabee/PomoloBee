from django.test import TestCase
from django.urls import reverse
import requests
from rest_framework import status
from core.models import Image,Farm, Field, Fruit, Row,User
from django.conf import settings

class MLIntegrationTest(TestCase):
    """Test interaction between Django and Flask ML API."""

    def setUp(self): 

        self.user = User.objects.create_user(username="testuser", password="testpass")

        self.farm = Farm.objects.create(name="Test Farm", owner=self.user)

 
        self.field = Field.objects.create(
            short_name="F1", name="Test Field", orientation="N", farm=self.farm
        )
        self.fruit = Fruit.objects.create(
            short_name="R", name="Red Fruit", description="",
            yield_start_date="2024-01-01", yield_end_date="2024-12-01",
            yield_avg_kg=2.5, fruit_avg_kg=0.3
        )
        self.row = Row.objects.create(
            short_name="R1", name="Row 1", nb_plant=30,
            field=self.field, fruit=self.fruit
)


        """Set up test data."""
        self.image = Image.objects.create(
            image_file="images/orchard.jpg",
            processed=False,
            row=self.row,
            date="2024-03-14"
        )

        self.ml_api_url = settings.ML_API_URL  # Ensure this is set in settings.py

    def test_django_sends_image_to_ml(self):
        """Test if Django correctly sends an image processing request to Flask."""
        payload = {
            "image_url": f"/media/{self.image.image_file}",
            "image_id": self.image.id
        }
        response = requests.post(f"{self.ml_api_url}/process-image", json=payload)

        self.assertEqual(response.status_code, 200)
        self.assertIn("message", response.json().get("data", {}))



    def test_ml_sends_results_back_to_django(self):
        """Test if Flask successfully sends ML results back to Django."""
        payload = {
            "nb_fruit": 15,
            "confidence_score": 0.85,
            "processed": True
        }
        response = self.client.post(
            reverse("ml-result", args=[self.image.id]), data=payload, content_type="application/json"
        )

        self.assertEqual(response.status_code, 200)
        self.assertIn("message", response.json().get("data", {}))

        # Ensure image history is updated
        self.image.refresh_from_db()
        self.assertEqual(self.image.nb_fruit, 15)
        self.assertEqual(self.image.confidence_score, 0.85)
        self.assertTrue(self.image.processed)

    def test_django_fetches_ml_results(self):
        """Test if Django correctly retrieves ML results after processing."""
        self.image.nb_fruit = 10
        self.image.confidence_score = 0.9
        self.image.processed = True
        self.image.save()

        response = self.client.get(reverse("ml-result", args=[self.image.id]))

        self.assertEqual(response.status_code, 200) 

        data = response.json()["data"]
        self.assertEqual(data["nb_fruit"], 10)
        self.assertEqual(data["confidence_score"], 0.9)

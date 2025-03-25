from django.test import TestCase
from django.urls import reverse
import requests
from rest_framework import status
from core.models import ImageHistory
from django.conf import settings

class MLIntegrationTest(TestCase):
    """Test interaction between Django and Flask ML API."""

    def setUp(self):
        """Set up test data."""
        self.image = ImageHistory.objects.create(
            image_path="test_image.jpg",
            processed=False
        )
        self.ml_api_url = settings.ML_API_URL  # Ensure this is set in settings.py

    def test_django_sends_image_to_ml(self):
        """Test if Django correctly sends an image processing request to Flask."""
        payload = {
            "image_url": f"{settings.MEDIA_URL}{self.image.image_path}",
            "image_id": self.image.id
        }
        response = requests.post(f"{self.ml_api_url}/process", json=payload)

        self.assertEqual(response.status_code, 200)
        self.assertIn("message", response.json())

    def test_ml_sends_results_back_to_django(self):
        """Test if Flask successfully sends ML results back to Django."""
        payload = {
            "nb_apples": 15,
            "confidence_score": 0.85,
            "processed": True
        }
        response = self.client.post(
            reverse("ml-result", args=[self.image.id]), data=payload, content_type="application/json"
        )

        self.assertEqual(response.status_code, 200)
        self.assertIn("message", response.json())

        # Ensure image history is updated
        self.image.refresh_from_db()
        self.assertEqual(self.image.nb_apfel, 15)
        self.assertEqual(self.image.confidence_score, 0.85)
        self.assertTrue(self.image.processed)

    def test_django_fetches_ml_results(self):
        """Test if Django correctly retrieves ML results after processing."""
        self.image.nb_apfel = 10
        self.image.confidence_score = 0.9
        self.image.processed = True
        self.image.save()

        response = self.client.get(reverse("ml-result", args=[self.image.id]))

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["nb_apples"], 10)
        self.assertEqual(response.json()["confidence_score"], 0.9)

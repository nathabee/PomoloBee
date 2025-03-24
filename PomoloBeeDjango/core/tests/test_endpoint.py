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
    def test_post_image_upload(self):
        # POST /api/images/
        # Add upload logic and assertions
        pass

    # Checking Processing Status
    def test_get_image_status(self):
        # GET /api/images/{image_id}/status/
        pass

    # Fetching Estimation Results
    def test_get_estimation_results(self):
        # GET /api/estimations/{image_id}/
        pass

    def test_get_latest_estimations(self):
        # GET /api/latest_estimations/
        pass

    # Fetching Image List
    def test_get_image_list(self):
        # GET /api/images/
        pass

    # Fetching Image Details
    def test_get_image_details(self):
        # GET /api/images/{image_id}/details/
        pass

    # Deleting an Image
    def test_delete_image(self):
        # DELETE /api/images/{image_id}/
        pass

    # Fetching Processing Errors
    def test_get_error_log(self):
        # GET /api/images/{image_id}/error_log
        pass

    # Fetching History of Estimations
    def test_get_history(self):
        # GET /api/history/
        pass

    def test_get_history_detail(self):
        # GET /api/history/{history_id}/
        pass

    # Request Retry for ML Processing
    def test_post_retry_processing(self):
        # POST /api/retry_processing/
        pass

    # App fetches ML results (Django-side)
    def test_get_ml_result_from_django(self):
        # GET /api/images/{image_id}/ml_result
        pass

    
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



    # ML → Django: Posting ML result
    def test_post_ml_result_to_django(self):
        # POST /api/images/{image_id}/ml_result
        pass

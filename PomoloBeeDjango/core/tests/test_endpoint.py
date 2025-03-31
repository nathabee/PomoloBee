from django.test import TestCase
from django.urls import reverse
from rest_framework import status
from django.contrib.auth.models import User
from core.models import Field, Fruit, Row, Farm, Image, Estimation
from django.core.files.uploadedfile import SimpleUploadedFile
from io import BytesIO
from PIL import Image as PILImage
from datetime import date


class APITest(TestCase):

    def setUp(self):
        self.user = User.objects.create_user(username='testuser', password='testpass')
        self.farm = Farm.objects.create(name="Test Farm", owner=self.user)

        self.field = Field.objects.create(
            short_name="North_Field", name="North Orchard",
            description="Test orchard", orientation="N", farm=self.farm
        )
        self.fruit = Fruit.objects.create(
            short_name="Red", name="Red Fruit", description="Red juicy fruit",
            yield_start_date="2024-08-01", yield_end_date="2024-09-01",
            yield_avg_kg=2.5, fruit_avg_kg=0.3
        )
        self.row = Row.objects.create(
            short_name="Row_A", name="Row A", nb_plant=50,
            fruit=self.fruit, field=self.field
        )

    def test_get_fields(self):
        response = self.client.get(reverse("fields-list"))
        self.assertEqual(response.status_code, 200)
        self.assertIn("fields", response.json()["data"])

    def test_get_fruits(self):
        response = self.client.get(reverse("fruits-list"))
        self.assertEqual(response.status_code, 200)
        self.assertIn("fruits", response.json()["data"])

    def test_get_locations(self):
        response = self.client.get(reverse("locations"))
        self.assertEqual(response.status_code, 200)
        self.assertIn("locations", response.json()["data"])

    def test_upload_image(self):
        image_file = BytesIO()
        PILImage.new("RGB", (100, 100)).save(image_file, format="JPEG")
        image_file.seek(0)
        uploaded_file = SimpleUploadedFile("test.jpg", image_file.read(), content_type="image/jpeg")

        response = self.client.post(reverse("image-upload"), {
            "image": uploaded_file,
            "row_id": self.row.id,
            "date": "2024-03-25"
        })

        self.assertEqual(response.status_code, 201)
        self.assertIn("image_id", response.json()["data"])

    def test_get_image_details(self):
        image = Image.objects.create(row=self.row, date="2024-03-25")
        response = self.client.get(reverse("image-detail", args=[image.id]))
        self.assertEqual(response.status_code, 200)

    def test_delete_image(self):
        image = Image.objects.create(row=self.row, date="2024-03-25")
        response = self.client.delete(reverse("image-delete", args=[image.id]))
        self.assertEqual(response.status_code, 200)
        self.assertFalse(Image.objects.filter(id=image.id).exists())
        
    def test_post_retry_processing(self):
        # Generate a dummy image file
        image_file = BytesIO()
        PILImage.new("RGB", (100, 100)).save(image_file, format="JPEG")
        image_file.seek(0)
        uploaded_file = SimpleUploadedFile("retry.jpg", image_file.read(), content_type="image/jpeg")

        # Upload via the API to simulate the real flow
        upload_response = self.client.post(reverse("image-upload"), {
            "image": uploaded_file,
            "row_id": self.row.id,
            "date": "2024-03-25"
        })

        self.assertEqual(upload_response.status_code, 201)
        image_id = upload_response.json()["data"]["image_id"]

        # Now call the retry endpoint
        response = self.client.post(reverse("retry-processing"), {
            "image_id": image_id
        }, content_type="application/json")

        self.assertIn(response.status_code, [200, 503])


    def test_get_image_estimation(self):
        image = Image.objects.create(row=self.row, date="2024-03-25", processed=True)
        Estimation.objects.create(
            image=image, row=self.row, date="2024-03-25",
            plant_fruit=10, estimated_yield_kg=50, plant_kg=3.0, row_kg=150.0,
            confidence_score=0.9, source="MLI"
        )
        response = self.client.get(reverse("image-estimations", args=[image.id]))
        self.assertEqual(response.status_code, 200)

    def test_get_field_estimations(self):
        image = Image.objects.create(row=self.row, date="2024-03-25", processed=True)
        Estimation.objects.create(
            image=image, row=self.row, date="2024-03-25",
            plant_fruit=10, estimated_yield_kg=50, plant_kg=3.0, row_kg=150.0,
            confidence_score=0.9, source="MLI"
        )
        response = self.client.get(reverse("field-estimations", args=[self.field.id]))
        self.assertEqual(response.status_code, 200)

    def test_post_ml_result(self):
        image = Image.objects.create(row=self.row, date="2024-03-25")
        response = self.client.post(
            reverse("ml-result", args=[image.id]),
            {
                "nb_fruit": 15,
                "confidence_score": 0.88,
                "processed": True
            },
            content_type="application/json"
        )
        self.assertEqual(response.status_code, 200)
        image.refresh_from_db()
        self.assertTrue(image.processed)

    def test_get_ml_version(self):
        response = self.client.get(reverse("ml-version"))
        self.assertEqual(response.status_code, 200)
        self.assertIn("model_version", response.json()["data"])


class RobustnessTest(TestCase):

    def setUp(self):
        self.user = User.objects.create_user(username='robustuser', password='testpass')
        self.farm = Farm.objects.create(name="Robust Farm", owner=self.user)
        self.field = Field.objects.create(short_name="R1", name="Field R1", orientation="E", farm=self.farm)
        self.fruit = Fruit.objects.create(
            short_name="TestFruit", name="Test Fruit",
            yield_start_date="2024-01-01", yield_end_date="2024-12-01",
            yield_avg_kg=3.0, fruit_avg_kg=0.4
        )
        self.row = Row.objects.create(short_name="R1-A", name="R1-A", field=self.field, fruit=self.fruit, nb_plant=30)

    def test_get_estimation_result_not_found(self):
        image = Image.objects.create(row=self.row, date="2024-03-25", processed=False)
        response = self.client.get(reverse("image-estimations", args=[image.id]))
        self.assertEqual(response.status_code, 404)
        error = response.json()["error"]
        self.assertEqual(error["code"], "404_NOT_FOUND")
        self.assertIn("Estimation not found", error["message"])
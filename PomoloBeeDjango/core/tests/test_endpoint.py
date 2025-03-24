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
        self.assertEqual(len(response.json()), 2)
        self.assertEqual(response.json()[0]["name"], "North Orchard")

    def test_get_fruits(self):
        response = self.client.get(reverse('fruits-list'))
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.json()), 2)
        self.assertEqual(response.json()[0]["name"], "Golden Apple")

    def test_get_locations(self):
        response = self.client.get(reverse('locations'))
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("locations", response.json())
        self.assertEqual(len(response.json()["locations"]), 2)
        self.assertEqual(len(response.json()["locations"][0]["raws"]), 2)

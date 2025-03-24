# Unit test: Database fixtures

from django.test import TestCase
from django.urls import reverse
from rest_framework import status
from core.models import Field, Fruit, Raw

class LoadFixtureDataTest(TestCase):
    """Test if initial fixture data is correctly loaded in the test database."""

    fixtures = ["initial_farms.json","initial_fields.json", "initial_fruits.json", "initial_raws.json"]

    def test_field_count(self):
        """Check if 6 fields are correctly loaded."""
        field_count = Field.objects.count()
        self.assertEqual(field_count, 6, "Expected 6 fields in the database.")

    def test_fruit_count(self):
        """Check if 6 fruits are correctly loaded."""
        fruit_count = Fruit.objects.count()
        self.assertEqual(fruit_count, 6, "Expected 6 fruits in the database.")

    def test_raw_count(self):
        """Check if 28 raws are correctly loaded."""
        raw_count = Raw.objects.count()
        self.assertEqual(raw_count, 28, "Expected 28 raws in the database.")

    def test_specific_field_data(self):
        """Verify specific field data (ChampMaison - C1)."""
        field = Field.objects.get(short_name="C1")
        self.assertEqual(field.name, "ChampMaison")
        self.assertEqual(field.description, "Champ situé sur la parcelle de la maison")
        self.assertEqual(field.orientation, "NW")

    def test_specific_fruit_data(self):
        """Verify specific fruit data (Swing_CG1)."""
        fruit = Fruit.objects.get(short_name="Swing_CG1")
        self.assertEqual(fruit.name, "Cultivar Swing on CG1")
        self.assertEqual(fruit.description, "Late harvest, sweet, crisp texture, medium storage (3-4 months), aromatic")
        self.assertEqual(fruit.yield_start_date.strftime("%Y-%m-%d"), "2025-09-15")
        self.assertEqual(fruit.yield_end_date.strftime("%Y-%m-%d"), "2025-10-05")
        self.assertEqual(fruit.yield_avg_kg, 40.0)
        self.assertEqual(fruit.fruit_avg_kg, 0.2)

    def test_specific_raw_data(self):
        """Verify specific raw data (C1-R3)."""
        raw = Raw.objects.get(short_name="C1-R3")
        self.assertEqual(raw.name, "Rang 3 cote maison Swing 3")
        self.assertEqual(raw.nb_plant, 40)
        self.assertEqual(raw.field.id, 1)  # Foreign key to Field
        self.assertEqual(raw.fruit.id, 1)  # Foreign key to Fruit

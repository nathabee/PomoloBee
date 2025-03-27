from django.test import TestCase
from django.contrib.auth.models import User
from core.models import Image,  Estimation, Raw, Fruit, Field, Farm
from datetime import date
from django.core.files.uploadedfile import SimpleUploadedFile

class LoadFixtureDataTest(TestCase):
    """Test if initial fixture data is correctly loaded in the test database."""

    fixtures = [
        "initial_superuser.json",
        "initial_farms.json",
        "initial_fields.json",
        "initial_fruits.json",
        "initial_raws.json"
    ]

    def test_superuser_exists(self):
        """Check if superuser 'pomobee' exists."""
        user_exists = User.objects.filter(username="pomobee").exists()
        self.assertTrue(user_exists, "Expected superuser 'pomobee' to exist in the database.")

    def test_farms_count(self):
        """Check that at least 1 farm is loaded from fixture."""
        farm_count = Farm.objects.count()
        self.assertGreaterEqual(farm_count, 1, "Expected at least 1 Farm in the database.")

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



class ModelTableExistenceTest(TestCase):
    # check existence of table that are not filled with data with fixture

    def setUp(self):
        # Create superuser (owner of the farm)
        self.user = User.objects.create_superuser(username="admin", password="adminpass", email="admin@example.com")
        
        # Create Farm with owner
        self.farm = Farm.objects.create(name="TestFarm", owner=self.user)
        
        # Create related Field and Fruit
        self.field = Field.objects.create(short_name="TestField", name="Test Field", farm=self.farm)
        self.fruit = Fruit.objects.create(
            short_name="Apple",
            name="Apple",
            description="Test apple",
            yield_start_date="2024-01-01",
            yield_end_date="2024-12-31",
            yield_avg_kg=2.0,
            fruit_avg_kg=0.3
        )
        
        # Create Raw for test
        self.raw = Raw.objects.create(
            field=self.field,
            fruit=self.fruit,
            name="Row A",
            short_name="RA",
            nb_plant=50
        )

    def test_image_table(self):
        self.assertEqual(Image.objects.count(), 0)
        
        fake_image = SimpleUploadedFile(
            name='test.jpg',
            content=b'\x47\x49\x46\x38\x89\x61',  # Just some fake image bytes
            content_type='image/jpeg'
        )

        img = Image.objects.create(
            image_file=fake_image,
            raw=self.raw,
            date=date.today()
        )

        self.assertEqual(Image.objects.count(), 1)
 
    def test_estimation_computes_plant_and_raw_kg_on_save(self):
        # Create a test image for linkage (optional, but model allows null)
        image = Image.objects.create(image_file="test.jpg", raw=self.raw, date=date.today())

        # Create estimation with only raw, image, plant_fruit — the rest should be auto-calculated
        estimation = Estimation.objects.create(
            image=image,
            raw=self.raw,
            date=date.today(),
            plant_fruit=12,  # 👈 Set fruit count per plant
            estimated_yield_kg=0,  # will override in save()
            maturation_grade=0.5,
            confidence_score=0.9,
            source=Estimation.EstimationSource.IMAGE
        )

        fruit_avg_kg = self.raw.fruit.fruit_avg_kg
        nb_plant = self.raw.nb_plant

        expected_plant_kg = 12 * fruit_avg_kg
        expected_raw_kg = expected_plant_kg * nb_plant

        self.assertAlmostEqual(estimation.plant_kg, expected_plant_kg, places=4)
        self.assertAlmostEqual(estimation.raw_kg, expected_raw_kg, places=4)
        self.assertEqual(estimation.image, image)
        self.assertEqual(estimation.raw, self.raw)
        self.assertEqual(estimation.source, "MLI")
        self.assertEqual(str(estimation), f"Estimation {estimation.id} - {self.raw.name} on {estimation.date}")



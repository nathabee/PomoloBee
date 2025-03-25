from django.test import TestCase
from django.contrib.auth.models import User
from core.models import ImageHistory, HistoryRaw, HistoryEstimation, Raw, Fruit, Field, Farm
from datetime import date


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
        self.field = Field.objects.create(short_name="TestField", name="Test Field", id_farm=self.farm)
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

    def test_image_history_table(self):
        self.assertEqual(ImageHistory.objects.count(), 0)
        img = ImageHistory.objects.create(image_path="test.jpg", raw=self.raw, date=date.today())
        self.assertEqual(ImageHistory.objects.count(), 1)

    def test_history_raw_table(self):
        img = ImageHistory.objects.create(image_path="img.jpg", raw=self.raw, date=date.today())
        hist = HistoryRaw.objects.create(date=date.today(), raw=self.raw, id_image=img, plant_apfel=10)
        self.assertAlmostEqual(hist.raw_kg, hist.plant_apfel * self.fruit.fruit_avg_kg * self.raw.nb_plant)

    def test_history_estimation_table(self):
        estimation = HistoryEstimation.objects.create(
            date=date.today(),
            raw=self.raw,
            estimated_yield_kg=48.5,
            confidence_score=0.82
        )
        self.assertEqual(estimation.estimated_yield_kg, 48.5)
        self.assertEqual(HistoryEstimation.objects.count(), 1)


# check that History raw is created each time we create a Image History with process = True
class AutoHistoryCreationTest(TestCase):
    
    fixtures = [
        "initial_superuser.json",      
        "initial_farms.json",
        "initial_fields.json",
        "initial_fruits.json",
        "initial_raws.json"
    ]
    
    def test_history_raw_created_after_ml_result(self):
        raw = Raw.objects.first()
        image = ImageHistory.objects.create(
            image_path="test.jpg",
            raw=raw,
            date=date.today(),
            nb_apfel=10,
            confidence_score=0.85,
            processed=True  # 👈 Trigger signal
        )

        self.assertTrue(HistoryRaw.objects.filter(id_image=image).exists())
        self.assertTrue(HistoryEstimation.objects.filter(raw=raw).exists())
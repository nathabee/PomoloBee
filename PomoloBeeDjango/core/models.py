from django.db import models
from django.contrib.auth.models import User


# Defines a Fruit Type
class Fruit(models.Model):
    short_name = models.CharField(max_length=50, unique=True)
    name = models.CharField(max_length=255)
    description = models.TextField(blank=True, null=True)
    yield_start_date = models.DateField()
    yield_end_date = models.DateField()
    yield_avg_kg = models.FloatField()  # Average yield per plant
    fruit_avg_kg = models.FloatField()  # Average weight of individual fruit

    def __str__(self):
        return self.name


# Defines a Farm
class Farm(models.Model):
    name = models.CharField(max_length=100)
    owner = models.ForeignKey(User, on_delete=models.CASCADE, related_name='farms')  # ✅ Renamed back to 'owner'

    def __str__(self):
        return f"Farm: {self.name}"


# Defines a Field (Agricultural Field)
class Field(models.Model):
    farm = models.ForeignKey(Farm, on_delete=models.CASCADE, related_name='fields', default=1)
    short_name = models.CharField(max_length=50, unique=True)
    name = models.CharField(max_length=255)
    description = models.TextField(blank=True, null=True)
    orientation = models.CharField(max_length=50, blank=True, null=True)

    def __str__(self):
        return self.name


# Defines a Raw (a section in a field)
class Raw(models.Model):
    short_name = models.CharField(max_length=50, unique=True)
    name = models.CharField(max_length=255)
    nb_plant = models.IntegerField()  # Number of plants in the raw
    fruit = models.ForeignKey('Fruit', on_delete=models.CASCADE, related_name='raws')
    field = models.ForeignKey('Field', on_delete=models.CASCADE, related_name='raws')

    def __str__(self):
        return self.name


# Image storage for estimation analysis
class Image(models.Model):
    STATUS_CHOICES = [
        ("processing", "Processing"),
        ("done", "Done"),
        ("failed", "Failed"),
        ("badjpg", "Invalid Image"),
    ]
    raw = models.ForeignKey('Raw', on_delete=models.CASCADE, related_name='images')
    date = models.DateField(null=True, blank=True)  # Date of capture (from app) (no time)
    upload_date =  models.DateField(null=True, blank=True)  # Date of upload in django (no time)
    image_file = models.ImageField(upload_to='images/')  # Stores uploaded image
    original_filename = models.CharField(max_length=255, blank=True, null=True) 

    # ML result
    nb_fruit = models.FloatField(null=True, blank=True)
    confidence_score = models.FloatField(null=True, blank=True)
    processed = models.BooleanField(default=False)
    status = models.CharField(
        max_length=20,
        choices=STATUS_CHOICES,
        default="processing"
    )
    processed_at = models.DateTimeField(null=True, blank=True)

    def __str__(self):
        return f"Image {self.id} - {self.raw.name if self.raw else 'No Raw'}"


# Estimation (Processed Data)
class Estimation(models.Model):
    image = models.ForeignKey(Image, on_delete=models.SET_NULL, null=True, related_name='estimations')
    raw = models.ForeignKey('Raw', on_delete=models.CASCADE, related_name='estimations')
    date = models.DateField()  # From user/app
    timestamp = models.DateTimeField(auto_now_add=True)
    plant_fruit = models.FloatField()
    plant_kg = models.FloatField()
    raw_kg = models.FloatField()
    estimated_yield_kg = models.FloatField()
    maturation_grade = models.FloatField(default=0)
    confidence_score = models.FloatField(null=True, blank=True)

    class EstimationSource(models.TextChoices):
        USER = "USR", "User manual estimation"
        IMAGE = "MLI", "Machine Learning (Image)"
        VIDEO = "MLV", "Machine Learning (Video)"

    source = models.CharField(
        max_length=3,
        choices=EstimationSource.choices,
        default=EstimationSource.IMAGE
    )

    def save(self, *args, **kwargs):
        if self.raw and self.raw.fruit:
            self.plant_kg = self.plant_fruit * self.raw.fruit.fruit_avg_kg
            self.raw_kg = self.plant_kg * self.raw.nb_plant
        super().save(*args, **kwargs)

    def __str__(self):
        return f"Estimation {self.id} - {self.raw.name} on {self.date}"

---
<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [Defines a Field Agricultural Field](#defines-a-field-agricultural-field)
- [Defines a Raw a section in a field](#defines-a-raw-a-section-in-a-field)
- [Defines a Fruit Type](#defines-a-fruit-type)
- [Image storage for estimation analysis](#image-storage-for-estimation-analysis)
- [History of Raw Analysis Processed Data](#history-of-raw-analysis-processed-data)
- [History of Yield Estimations](#history-of-yield-estimations)
- [Data example](#data-example)
<!-- TOC END -->
 
</details>
---
 
from django.db import models

# Defines a Field Agricultural Field
class Field(models.Model):
    id = models.AutoField(primary_key=True)
    short_name = models.CharField(max_length=50, unique=True)  # Unique short identifier
    name = models.CharField(max_length=255)
    description = models.TextField(blank=True, null=True)
    orientation = models.CharField(max_length=50, blank=True, null=True)  # N, S, E, W

    def __str__(self):
        return self.name

# Defines a Raw a section in a field
class Raw(models.Model):
    id = models.AutoField(primary_key=True)
    short_name = models.CharField(max_length=50, unique=True)
    name = models.CharField(max_length=255)
    nb_plant = models.IntegerField()  # Number of plants in the raw
    fruit = models.ForeignKey('Fruit', on_delete=models.CASCADE, related_name='raws')  # ✅ One raw → One fruit
    field = models.ForeignKey('Field', on_delete=models.CASCADE, related_name='raws')  # ✅ One field → Many raws



    def __str__(self):
        return self.name

# Defines a Fruit Type
class Fruit(models.Model):
    id = models.AutoField(primary_key=True)
    short_name = models.CharField(max_length=50, unique=True)
    name = models.CharField(max_length=255)
    description = models.TextField(blank=True, null=True)
    yield_start_date = models.DateField()  
    yield_end_date = models.DateField()
    yield_avg_kg = models.FloatField()  # Average yield per plant
    fruit_avg_kg = models.FloatField()  # Average weight of individual fruit

    def __str__(self):
        return self.name


# Image storage for estimation analysis
class ImageHistory(models.Model):
    id = models.AutoField(primary_key=True)
    image_path = models.CharField(max_length=255)  # Store path or URL of the image
    nb_apfel = models.FloatField(null=True, blank=True)  # Number of apples detected by ML
    confidence_score = models.FloatField(null=True, blank=True)  # Store ML confidence
    processed = models.BooleanField(default=False)  # Flag if ML has processed the image
    raw = models.ForeignKey('Raw', on_delete=models.CASCADE, related_name='images')  # ✅ New: Links image to a raw

    def __str__(self):
        return f"Image {self.id} - {self.raw.name if self.raw else 'No Raw'}"

 



# History of Raw Analysis Processed Data
class HistoryRaw(models.Model):
    id = models.AutoField(primary_key=True)
    id_image = models.ForeignKey(ImageHistory, on_delete=models.SET_NULL, null=True, related_name='history') 
    date = models.DateField()  # Given by frontend
    timestamp = models.DateTimeField(auto_now_add=True)  # Automatically set when record is created
    raw = models.ForeignKey('Raw', on_delete=models.CASCADE, related_name='history')

    # ML Output - Number of apples detected per plant
    plant_apfel = models.FloatField()

    # Calculation: Expected fruit weight per plant (ML detected apples * average fruit weight for this fruit type)
    plant_kg = models.FloatField()

    # Calculation: Expected total raw weight (plant_kg * number of plants in this raw)
    raw_kg = models.FloatField()

    # Placeholder for future maturation analysis
    maturation_grade = models.FloatField(default=0)

    def save(self, *args, **kwargs):
        """ Before saving, calculate plant_kg and raw_kg based on ML detection results. """
        if self.raw and self.raw.fruit:
            self.plant_kg = self.plant_apfel * self.raw.fruit.fruit_avg_kg
            self.raw_kg = self.plant_kg * self.raw.nb_plant
        super().save(*args, **kwargs)

    def __str__(self):
        return f"HistoryRaw {self.id} - {self.raw.name} on {self.date}"


# History of Yield Estimations
class HistoryEstimation(models.Model):
    id = models.AutoField(primary_key=True)
    date = models.DateField()
    timestamp = models.DateTimeField(auto_now_add=True)
    raw = models.ForeignKey(Raw, on_delete=models.CASCADE, related_name='estimations')
    estimated_yield_kg = models.FloatField()  # Predicted yield for the raw
    confidence_score = models.FloatField()  # AI confidence level (0-1)

    def __str__(self):
        return f"Estimation {self.id} - {self.raw.name}"


# Data example
Fruit :

| Description                      | Yield Start Date | Yield End Date | Yield Avg (kg/tree) | Fruit Avg (kg/fruit) |
|----------------------------------|-----------------|---------------|----------------------|----------------------|
| **Cultivar Swing on CG1**        | 2025-09-15  | 2025-10-05 | 40               | 0.2            |
| **Cultivar Ladina on CG1**       | 2025-09-25 | 2025-10-15   | 35               | 0.22            |
| **Cultivar Gallwa on CG1**       | 2025-09-15  | 2025-10-05 | 45               | 0.24            |
| **Cultivar Pitch on M9**         | 2025-09-01 |  2025-09-25| 55               | 0.18            |
| **Cultivar Pixie on CG1**        | 2025-09-25 | 2025-10-15    | 30               | 0.16            |
| **Cultivar Early Crunch on M9 Nakab** | 2025-08-05   | 2025-08-25    | 50               | 0.17            |

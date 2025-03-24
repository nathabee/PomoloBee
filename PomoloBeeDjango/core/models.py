from django.db import models
from django.contrib.auth.models import User


  
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

# define a farm
class Farm(models.Model):
    name = models.CharField(max_length=100)
    owner = models.ForeignKey(User, on_delete=models.CASCADE, related_name='farms')

    def __str__(self):
        return f"Farm: {self.name}"
    
# Defines a Field (Agricultural Field)
class Field(models.Model):
    id = models.AutoField(primary_key=True)
    id_farm = models.ForeignKey(
        Farm,
        on_delete=models.CASCADE,
        related_name='fields',
        default=1  
    )
    short_name = models.CharField(max_length=50, unique=True)
    name = models.CharField(max_length=255)
    description = models.TextField(blank=True, null=True)
    orientation = models.CharField(max_length=50, blank=True, null=True)

    def __str__(self):
        return self.name




# Defines a Raw (a section in a field)
class Raw(models.Model):
    id = models.AutoField(primary_key=True)
    short_name = models.CharField(max_length=50, unique=True)
    name = models.CharField(max_length=255)
    nb_plant = models.IntegerField()  # Number of plants in the raw
    fruit = models.ForeignKey('Fruit', on_delete=models.CASCADE, related_name='raws')  # ✅ One raw → One fruit
    field = models.ForeignKey('Field', on_delete=models.CASCADE, related_name='raws')  # ✅ One field → Many raws



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
    date = models.DateField(null=True, blank=True)
    def __str__(self):
        return f"Image {self.id} - {self.raw.name if self.raw else 'No Raw'}"

 



# History of Raw Analysis (Processed Data)
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
 

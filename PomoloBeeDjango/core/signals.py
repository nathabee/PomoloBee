from django.db.models.signals import post_save
from django.dispatch import receiver
from .models import ImageHistory, HistoryRaw, HistoryEstimation
from datetime import date
import logging

logger = logging.getLogger(__name__)
 
    
#create_estimation_and_history

@receiver(post_save, sender=ImageHistory)
def image_processed_handler(sender, instance, created, **kwargs):
    # print(f"🔔 Signal fired! Image ID: {instance.id}, Processed: {instance.processed}")
    if instance.processed and instance.nb_apfel is not None:
        logger.info(f"Triggering yield estimation for Image {instance.id}")
        # Avoid duplicate creation if history already exists
        if not HistoryRaw.objects.filter(id_image=instance).exists():
            raw = instance.raw
            fruit = raw.fruit
            plant_kg = instance.nb_apfel * fruit.fruit_avg_kg
            raw_kg = plant_kg * raw.nb_plant

            # Create HistoryRaw
            HistoryRaw.objects.create(
                id_image=instance,
                date=instance.date or date.today(),
                raw=raw,
                plant_apfel=instance.nb_apfel,
                plant_kg=plant_kg,
                raw_kg=raw_kg,
                maturation_grade=0
            )

            # Create HistoryEstimation
            HistoryEstimation.objects.create(
                date=instance.date or date.today(),
                raw=raw,
                estimated_yield_kg=raw_kg,
                confidence_score=instance.confidence_score or 0
            )

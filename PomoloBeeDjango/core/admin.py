from django.contrib import admin
from .models import Field, Raw, Fruit, Image, Estimation


@admin.register(Field)
class FieldAdmin(admin.ModelAdmin):
    list_display = ('name', 'short_name', 'orientation', 'description')
    search_fields = ('name', 'short_name')
    list_filter = ('orientation',)


@admin.register(Raw)
class RawAdmin(admin.ModelAdmin):
    list_display = ('name', 'short_name', 'nb_plant', 'field', 'fruit')
    search_fields = ('name', 'short_name')
    list_filter = ('field', 'fruit')


@admin.register(Fruit)
class FruitAdmin(admin.ModelAdmin):
    list_display = ('name', 'short_name', 'yield_start_date', 'yield_end_date', 'yield_avg_kg', 'fruit_avg_kg')
    search_fields = ('name', 'short_name')
    list_filter = ('yield_start_date', 'yield_end_date')


@admin.register(Image)
class ImageAdmin(admin.ModelAdmin):
    list_display = ('image_file', 'raw', 'date', 'nb_fruit', 'confidence_score', 'processed', 'processed_at')
    search_fields = ('image_file',)
    list_filter = ('processed', 'raw')


@admin.register(Estimation)
class EstimationAdmin(admin.ModelAdmin):
    list_display = (
        'image', 'date', 'raw', 'plant_fruit', 'plant_kg', 'raw_kg',
        'maturation_grade', 'estimated_yield_kg', 'confidence_score', 'source'
    )
    search_fields = ('raw__name',)
    list_filter = ('date', 'raw', 'source')

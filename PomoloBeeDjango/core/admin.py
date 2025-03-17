from django.contrib import admin
 
from .models import Field, Raw, Fruit, ImageHistory, HistoryRaw, HistoryEstimation


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


@admin.register(ImageHistory)
class ImageHistoryAdmin(admin.ModelAdmin):
    list_display = ('image_path', 'raw', 'nb_apfel', 'confidence_score', 'processed')
    search_fields = ('image_path',)
    list_filter = ('processed',)


@admin.register(HistoryRaw)
class HistoryRawAdmin(admin.ModelAdmin):
    list_display = ('id_image', 'date', 'raw', 'plant_apfel', 'plant_kg', 'raw_kg', 'maturation_grade')
    search_fields = ('raw__name',)
    list_filter = ('date', 'raw')


@admin.register(HistoryEstimation)
class HistoryEstimationAdmin(admin.ModelAdmin):
    list_display = ('raw', 'date', 'estimated_yield_kg', 'confidence_score')
    search_fields = ('raw__name',)
    list_filter = ('date', 'raw')



from django.contrib import admin
from .models import Field, Row, Fruit, Image, Estimation


@admin.register(Field)
class FieldAdmin(admin.ModelAdmin):
    list_display = ('name', 'short_name', 'orientation', 'description', 'svg_preview')
    search_fields = ('name', 'short_name')
    list_filter = ('orientation',)

    def svg_preview(self, obj):
        if obj.svg_map:
            return f"<a href='{obj.svg_map.url}' target='_blank'>Preview</a>"
        return "No SVG"
    svg_preview.allow_tags = True
    svg_preview.short_description = "SVG Map"


@admin.register(Row)
class RowAdmin(admin.ModelAdmin):
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
    list_display = ('image_file', 'row', 'date', 'nb_fruit', 'confidence_score', 'processed', 'processed_at')
    search_fields = ('image_file',)
    list_filter = ('processed', 'row')


@admin.register(Estimation)
class EstimationAdmin(admin.ModelAdmin):
    list_display = (
        'image', 'date', 'row', 'plant_fruit', 'plant_kg', 'row_kg',
        'maturation_grade', 'estimated_yield_kg', 'confidence_score', 'source'
    )
    search_fields = ('row__name',)
    list_filter = ('date', 'row', 'source')

from rest_framework import serializers
from .models import Field, Fruit, Raw, Image, Estimation


# FIELD
class FieldSerializer(serializers.ModelSerializer):
    field_id = serializers.IntegerField(source='id', read_only=True)

    class Meta:
        model = Field
        fields = ['field_id', 'short_name', 'name', 'description', 'orientation']


# FRUIT
class FruitSerializer(serializers.ModelSerializer):
    fruit_id = serializers.IntegerField(source='id', read_only=True)

    class Meta:
        model = Fruit
        fields = [
            'fruit_id', 'short_name', 'name', 'description',
            'yield_start_date', 'yield_end_date',
            'yield_avg_kg', 'fruit_avg_kg'
        ]


# RAW
class RawSerializer(serializers.ModelSerializer):
    raw_id = serializers.IntegerField(source='id', read_only=True)
    fruit_id = serializers.IntegerField(source='fruit.id', read_only=True)
    fruit_type = serializers.CharField(source='fruit.name', read_only=True)

    class Meta:
        model = Raw
        fields = ['raw_id', 'short_name', 'name', 'nb_plant', 'fruit_id', 'fruit_type']


# FIELD + NESTED RAWS
class FieldLocationSerializer(serializers.ModelSerializer):
    field_id = serializers.IntegerField(source='id', read_only=True)
    field_name = serializers.CharField(source='name', read_only=True)
    raws = RawSerializer(many=True, read_only=True)

    class Meta:
        model = Field
        fields = ['field_id', 'field_name', 'orientation', 'raws']


class ImageSerializer(serializers.ModelSerializer):
    image_id = serializers.IntegerField(source='id', read_only=True)
    raw_id = serializers.IntegerField(source='raw.id', read_only=True)
    field_id = serializers.IntegerField(source='raw.field.id', read_only=True)
    fruit_type = serializers.CharField(source='raw.fruit.name', read_only=True)
    image_url = serializers.SerializerMethodField()
    status = serializers.CharField(source='get_status_display', read_only=True)  # 🧠 This is the key line

    date = serializers.DateField(read_only=True, format="%Y-%m-%d")
    upload_date = serializers.DateField(read_only=True, format="%Y-%m-%d")
    processed_at = serializers.DateTimeField(read_only=True, format="%Y-%m-%dT%H:%M:%S")


    original_filename = serializers.CharField(read_only=True)

    def get_image_url(self, obj):
        request = self.context.get('request')
        if obj.image_file and request:
            return request.build_absolute_uri(obj.image_file.url)
        elif obj.image_file:
            return obj.image_file.url
        return None

    class Meta:
        model = Image
        fields = [
            'image_id', 'raw_id', 'field_id', 'fruit_type',
            'upload_date', 'date', 'image_url', 'original_filename',
            'processed', 'processed_at', 'nb_fruit', 'confidence_score', 'status'
        ]




# UPLOAD IMAGE
class ImageUploadSerializer(serializers.Serializer):
    image = serializers.ImageField()
    raw_id = serializers.IntegerField()
    date = serializers.DateField()


# ESTIMATION (History)
class EstimationSerializer(serializers.ModelSerializer):
    estimation_id = serializers.IntegerField(source='id', read_only=True)
    raw_id = serializers.IntegerField(source='raw.id', read_only=True)
    raw_name = serializers.CharField(source='raw.name', read_only=True)
    field_id = serializers.IntegerField(source='raw.field.id', read_only=True)
    field_name = serializers.CharField(source='raw.field.name', read_only=True)
    fruit_type = serializers.CharField(source='raw.fruit.name', read_only=True)
    image_id = serializers.IntegerField(source='image.id', read_only=True)
    confidence_score = serializers.FloatField()
    source = serializers.CharField(source='get_source_display', read_only=True)
    status = serializers.SerializerMethodField()
    timestamp = serializers.DateTimeField(read_only=True, format="%Y-%m-%dT%H:%M:%S")


    def get_status(self, obj):
        if obj.image and getattr(obj.image, 'status', None):
            return obj.image.status
        return "unknown"

    class Meta:
        model = Estimation
        fields = [
            'estimation_id', 'image_id', 'date', 'timestamp',
            'raw_id', 'raw_name', 'field_id', 'field_name', 'fruit_type',
            'plant_fruit', 'plant_kg', 'raw_kg', 'estimated_yield_kg',
            'maturation_grade', 'confidence_score', 'source', 'status'
        ]



# ML RESULT (Simplified)
class MLResultSerializer(serializers.ModelSerializer):
    image_id = serializers.IntegerField(source='id', read_only=True)

    class Meta:
        model = Image
        fields = ['image_id', 'nb_fruit', 'confidence_score', 'processed']

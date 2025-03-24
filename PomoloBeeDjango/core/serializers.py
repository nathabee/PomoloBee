from rest_framework import serializers
from .models import Field, Fruit, Raw,ImageHistory
 

class FieldSerializer(serializers.ModelSerializer):
    field_id = serializers.IntegerField(source='id', read_only=True)

    class Meta:
        model = Field
        fields = ['field_id', 'short_name', 'name', 'description', 'orientation']


class FruitSerializer(serializers.ModelSerializer):
    fruit_id = serializers.IntegerField(source='id', read_only=True)

    class Meta:
        model = Fruit
        fields = ['fruit_id', 'short_name', 'name', 'description',
                  'yield_start_date', 'yield_end_date', 'yield_avg_kg', 'fruit_avg_kg']


# api/location endpoint
class RawSerializer(serializers.ModelSerializer):
    raw_id = serializers.IntegerField(source='id', read_only=True)
    fruit_id = serializers.IntegerField(source='fruit.id', read_only=True)
    fruit_type = serializers.CharField(source='fruit.name', read_only=True)

    class Meta:
        model = Raw
        fields = ['raw_id', 'short_name', 'name', 'nb_plant', 'fruit_id', 'fruit_type']

class FieldLocationSerializer(serializers.ModelSerializer):
    field_id = serializers.IntegerField(source='id', read_only=True)
    field_name = serializers.CharField(source='name', read_only=True)
    raws = RawSerializer(many=True, read_only=True)  # Nested raws inside fields

    class Meta:
        model = Field
        fields = ['field_id', 'field_name', 'orientation', 'raws']



# PATCH : update raw or field
#class RawUpdateSerializer(serializers.ModelSerializer):
#    class Meta:
#        model = Raw
#        fields = ['name', 'nb_plant']

#class FieldUpdateSerializer(serializers.ModelSerializer):
#    class Meta:
#        model = Field
#        fields = ['name', 'orientation']

# api/images endpoint
 
class ImageSerializer(serializers.ModelSerializer):
    image_id = serializers.IntegerField(source='id', read_only=True)

    class Meta:
        model = ImageHistory
        fields = ['image_id', 'image_path', 'processed', 'nb_apfel', 'confidence_score', 'raw_id']

class ImageUploadSerializer(serializers.Serializer):
    image = serializers.ImageField()
    raw_id = serializers.IntegerField()
    date = serializers.DateField()

# api/history


# Serializer for fetching history records
class HistorySerializer(serializers.ModelSerializer):
    history_id = serializers.IntegerField(source='id', read_only=True)
    raw_id = serializers.IntegerField(source='raw.id', read_only=True)
    raw_name = serializers.CharField(source='raw.name', read_only=True)
    field_id = serializers.IntegerField(source='raw.field.id', read_only=True)
    field_name = serializers.CharField(source='raw.field.name', read_only=True)
    fruit_type = serializers.CharField(source='raw.fruit.name', read_only=True)

    class Meta:
        model = ImageHistory
        fields = ['history_id', 'raw_id', 'raw_name', 'field_id', 'field_name', 'fruit_type', 
                  'nb_apfel', 'confidence_score', 'image_path', 'processed']

# Serializer for fetching ML results of an image 
class MLResultSerializer(serializers.ModelSerializer):
    image_id = serializers.IntegerField(source='id', read_only=True)

    class Meta:
        model = ImageHistory
        fields = ['image_id', 'nb_apfel', 'confidence_score', 'processed']

from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    FieldViewSet, FruitViewSet, LocationListView,  
    EstimationView,   ImageDetailView, ImageDeleteView,
    ImageView,  RetryProcessingView, 
    MLResultView, MLVersionView, FieldEstimationListView
)

router = DefaultRouter()
router.register(r'fields', FieldViewSet, basename='fields')
router.register(r'fruits', FruitViewSet, basename='fruits')

urlpatterns = [
    path('', include(router.urls)),
    path('locations/', LocationListView.as_view(), name='locations'),
    path('images/', ImageView.as_view(), name='image-upload'),  # ✅ POST /api/images/
    path('images/<int:image_id>/details/', ImageDetailView.as_view(), name='image-detail'),  # ✅ GET /details
    path('images/<int:image_id>/', ImageDeleteView.as_view(), name='image-delete'),  # ✅ DELETE
    path('images/<int:image_id>/estimations/', EstimationView.as_view(), name='image-estimations'),  # ✅ GET
    path('fields/<int:field_id>/estimations/', FieldEstimationListView.as_view(), name='field-estimations'),  # ✅ GET
    path('images/<int:image_id>/ml_result/', MLResultView.as_view(), name='ml-result'),  # ✅ POST from ML
    path('retry_processing/', RetryProcessingView.as_view(), name='retry-processing'),  # ✅ POST from App
    path('ml/version/', MLVersionView.as_view(), name='ml-version'),  # ✅ GET from App
]

from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    FieldViewSet, FruitViewSet, LocationListView, ImageUploadView, ImageStatusView,
    EstimationView, LatestEstimationView, ImageListView, ImageDetailView, ImageDeleteView,
    HistoryView, HistoryDetailView, ErrorLogView, RetryProcessingView, 
      MLResultView, MLVersionView,ProcessImageView
)
 
router = DefaultRouter()
router.register(r'fields', FieldViewSet, basename='fields')
router.register(r'fruits', FruitViewSet, basename='fruits') 

urlpatterns = [
    path('', include(router.urls)),
    path('locations/', LocationListView.as_view(), name='locations'),
    path('images/', ImageListView.as_view(), name='image-list'),
    path('images/', ImageUploadView.as_view(), name='image-upload'),
    path('images/<int:image_id>/details/', ImageDetailView.as_view(), name='image-detail'),
    path('images/<int:image_id>/status/', ImageStatusView.as_view(), name='image-status'),
    path('images/<int:image_id>/error_log/', ErrorLogView.as_view(), name='image-error-log'),
    path('images/<int:image_id>/', ImageDeleteView.as_view(), name='image-delete'),
    path('estimations/<int:image_id>/', EstimationView.as_view(), name='estimation-detail'),
    path('latest_estimations/', LatestEstimationView.as_view(), name='latest-estimations'),
    path('history/', HistoryView.as_view(), name='history-list'),
    path('history/<int:history_id>/', HistoryDetailView.as_view(), name='history-detail'),
#    path('raws/<int:raw_id>/', RawUpdateView.as_view(), name='update-raw'),
#    path('fields/<int:field_id>/', FieldUpdateView.as_view(), name='update-field'),
    path('images/<int:image_id>/ml_result/', MLResultView.as_view(), name='ml-result'),  #get from app-- post from ML

    path('process-image/', ProcessImageView.as_view(), name='process-image'),  #post from app
    path('retry_processing/', RetryProcessingView.as_view(), name='retry-processing'),  #get from app
    path('ml/version/', MLVersionView.as_view(), name='ml-version'),   #get from app
]



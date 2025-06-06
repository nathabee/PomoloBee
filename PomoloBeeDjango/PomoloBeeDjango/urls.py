"""
URL configuration for PomoloBeeDjango project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/5.1/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path, include
from drf_spectacular.views import SpectacularAPIView, SpectacularSwaggerView
 
from django.conf.urls import handler404, handler500
from django.http import JsonResponse

def custom_404(request, exception=None):
    return JsonResponse({
        "error": {
            "code": "404_NOT_FOUND",
            "message": "The requested endpoint does not exist."
        }
    }, status=404)

def custom_500(request):
    return JsonResponse({
        "error": {
            "code": "500_INTERNAL_ERROR",
            "message": "An unexpected error occurred."
        }
    }, status=500)

handler404 = custom_404
handler500 = custom_500


urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/schema/', SpectacularAPIView.as_view(), name='schema'),
    path('api/docs/', SpectacularSwaggerView.as_view(url_name='schema'), name='swagger-ui'),
    path('api/', include('core.urls')),  # Include core app URLs under the "api/" prefix


]

from django.conf import settings
from django.conf.urls.static import static

# Serve media files **only in development**
if settings.BYPASS_MEDIA:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
    
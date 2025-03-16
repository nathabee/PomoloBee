# **📜 API Interface Definition**


---
## Table of Content
<!-- TOC -->
- [**📜 API Interface Definition**](#api-interface-definition)
  - [Table of Content](#table-of-content)
    - [**Introduction**  ](#introduction)
  - [**1️⃣ API DJANGO <-> APP**  ](#1-api-django--app)
  - [**2️⃣ API DJANGO <-> ML**  ](#2-api-django--ml)
  - [**3️⃣ Process Image API (Django to ML)**  ](#3-process-image-api-django-to-ml)
  - [**5️⃣ ML Processing Status API**  ](#5-ml-processing-status-api)
    - [**📌 Updated API Endpoints (`views.py`)**](#updated-api-endpoints-viewspy)
<!-- TOC END -->
---

### **Introduction**  
This document defines the API interface for the Pomolobee project, specifying:  
- API calls and data exchanged  
- Processing flow for Django, ML, and App interactions  

---

## **1️⃣ API DJANGO <-> APP**  
**Endpoints for communication between Django and the App:**  
- `POST /api/images/` → Uploads an image & starts ML processing  
- `GET /api/images/{image_id}/status` → Checks if ML has processed the image  
- `GET /api/estimations/{image_id}` → Retrieves the estimation results  

| `GET /api/fields/` | fetch all fields |
| `GET /api/fields/{field_id}/raws/` |to fetch raws for a given field | 

---

## **2️⃣ API DJANGO <-> ML**  
**Endpoints for communication between Django and the ML model:**  
- `POST /process-image/` → Django sends an image to ML for processing  
- ML returns: `nb_apfel` (number of apples detected) and `confidence_score`  

---

## **3️⃣ Process Image API (Django to ML)**  
📌 **Step 1: App uploads image** → `POST /api/images/`  
📌 **Step 2: Django sends image to ML API** → `POST /process-image/`  
📌 **Step 3: ML detects apples & returns results**  

✅ **Django View: Sends Image to ML API**
 
```python
def process_image(request):
    if request.method == 'POST' and request.FILES.get('image'):
        files = {'image': request.FILES['image']}
        response = requests.post(ML_API_URL, files=files)

        if response.status_code == 200:
            ml_data = response.json()

            # Update ImageHistory with ML results
            image_history = ImageHistory.objects.get(id=image_id)  
            image_history.nb_apfel = ml_data.get("nb_apfel")
            image_history.confidence_score = ml_data.get("confidence_score")
            image_history.processed = True
            image_history.save()

            return JsonResponse(ml_data)
        else:
            return JsonResponse({"error": "ML processing failed"}, status=500)
```

---

## **5️⃣ ML Processing Status API**  
The **app will periodically check** if ML has processed the image.  

✅ **API: `GET /api/images/{image_id}/status`**
```python
def get_ml_status(request, image_id):
    try:
        image_history = ImageHistory.objects.get(id=image_id)
        return JsonResponse({
            "processed": image_history.processed,
            "nb_apfel": image_history.nb_apfel,
            "confidence_score": image_history.confidence_score
        })
    except ImageHistory.DoesNotExist:
        return JsonResponse({"error": "Image not found"}, status=404)
```

--- 


### **📌 Updated API Endpoints (`views.py`)**
#### **1️⃣ Fetch All Fields**
```python
from django.http import JsonResponse
from .models import Field

def get_fields(request):
    fields = Field.objects.all().values("id", "short_name", "name", "description", "orientation")
    return JsonResponse({"fields": list(fields)})
```
✅ **API Endpoint:**
```
GET /api/fields/
```

---

#### **2️⃣ Fetch All Raws for a Given Field**
```python
from django.http import JsonResponse
from .models import Raw

def get_raws_by_field(request, field_id):
    raws = Raw.objects.filter(field_id=field_id).values("id", "short_name", "name", "nb_plant", "fruit_id")
    return JsonResponse({"raws": list(raws)})
```
✅ **API Endpoint:**
```
GET /api/fields/{field_id}/raws/
```

---


#### **3️⃣ Modify Image Upload API to Require `raw_id`**
```python
from django.views.decorators.csrf import csrf_exempt
import json

@csrf_exempt
def upload_image(request):
    if request.method == 'POST':
        try:
            image_file = request.FILES.get('image')
            raw_id = request.POST.get('raw_id')

            if not image_file or not raw_id:
                return JsonResponse({"error": "Image and raw_id are required"}, status=400)

            image_history = ImageHistory.objects.create(
                image_path=f"/images/{image_file.name}",
                raw_id=raw_id
            )

            return JsonResponse({"message": "Image uploaded successfully", "image_id": image_history.id})

        except Exception as e:
            return JsonResponse({"error": str(e)}, status=500)
```
✅ **API Endpoint:**
```
POST /api/images/
```
✅ **Updated Payload:**
```json
{
    "image": "apple_picture.jpg",
    "raw_id": 3
}
```

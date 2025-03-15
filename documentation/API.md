# **📜 API Interface Definition**

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
import requests
from django.http import JsonResponse

ML_API_URL = "http://ml-server:5001/process-image"

def process_image(request):
    if request.method == 'POST' and request.FILES.get('image'):
        files = {'image': request.FILES['image']}
        response = requests.post(ML_API_URL, files=files)

        if response.status_code == 200:
            ml_data = response.json()

            # Save ML results in ImageHistory
            image_history = ImageHistory.objects.create(
                image_path=f"/images/{request.FILES['image'].name}",
                nb_apfel=ml_data.get("nb_apfel"),
                confidence_score=ml_data.get("confidence_score"),
                processed=True
            )
            return JsonResponse(ml_data)
        else:
            return JsonResponse({"error": "ML processing failed"}, status=500)
```

---

## **4️⃣ Store `confidence_score` in Database**  
Your current ML API **returns confidence_score**, but Django needs to store it.

✅ **Fix: Update ImageHistory Model**
```python
class ImageHistory(models.Model):
    id = models.AutoField(primary_key=True)
    image_path = models.CharField(max_length=255)  # Store path or URL of the image
    nb_apfel = models.FloatField(null=True, blank=True)  # Apples detected by ML
    confidence_score = models.FloatField(null=True, blank=True)  # ML confidence score
    processed = models.BooleanField(default=False)  # Has the ML processed this image?

    def __str__(self):
        return f"Image {self.id} - {self.image_path}"
```

✅ **Fix: Store confidence_score after ML Processing**
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
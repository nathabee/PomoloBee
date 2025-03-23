## **5. Django ↔ ML API Integration**

📌 **Django sends image to a separate ML API (Flask/FastAPI, etc.)**  
✅ Better for scaling & performance  


---
<details>
<summary>Table of Content</summary>

<!-- TOC -->
  - [**5. Django ↔ ML API Integration**](#5-django-ml-api-integration)
    - [**Overview**](#overview)
    - [**Step 1 Create ML API Flask/FastAPI**](#step-1-create-ml-api-flaskfastapi)
    - [**Step 2 Django Calls ML API**](#step-2-django-calls-ml-api)
<!-- TOC END -->
 
</details>
---

 
### **Overview**
📌 Step 1: App uploads image, gets an `id`.  
📌 Step 2: ML processes the image asynchronously.  
📌 Step 3: App requests estimation later.  

---

### **Step 1 Create ML API Flask/FastAPI**
```python
# Flask API for ML
from flask import Flask, request, jsonify
import cv2
import numpy as np
from ml_model import detect_apples  # ML function

app = Flask(__name__)

@app.route('/process-image', methods=['POST'])
def process_image():
    file = request.files['image']
    img_array = np.asarray(bytearray(file.read()), dtype=np.uint8)
    img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

    # Run ML Model
    nb_apfel, confidence_score = detect_apples(img)

    return jsonify({"nb_apfel": nb_apfel, "confidence_score": confidence_score})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001)  # ML runs on separate port
```

---

### **Step 2 Django Calls ML API**
```python
# Django View Calls External ML API
import requests
from django.http import JsonResponse

ML_API_URL = "http://ml-server:5001/process-image"

def process_image(request):
    if request.method == 'POST' and request.FILES.get('image'):
        files = {'image': request.FILES['image']}
        response = requests.post(ML_API_URL, files=files)

        if response.status_code == 200:
            ml_data = response.json()
            return JsonResponse(ml_data)
        else:
            return JsonResponse({"error": "ML processing failed"}, status=500)
```

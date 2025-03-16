# Initialisation history 
- This document is used for me to trace how i created this repository initialisation when all project was empty, for the next time i make a project. It serves only information purposes.

 

You're setting up **three development projects**:  
✔ **PomologieDjango** (Django Backend)  
✔ **PomologieML** (ML API with Flask/FastAPI)  
✔ **PomologieApp** (Android Studio)    

in this doc we explain, the original setup of
| **Project**         | **Technology** | **Setup Command** |
|--------------------|--------------|----------------|
| **PomologieML** (ML API) | Flask + OpenCV | `python app.py` |

--- 
## **2️⃣ Initializing PomologieML (ML API with Flask/FastAPI)**
This will be your **machine learning API** that:  
✅ Receives images from Django 🖼️  
✅ Runs apple detection 🍏  
✅ Returns **number of apples (`nb_apfel`) & confidence score** 🤖  

---

### **📌 Step 1: Create PomologieML Project**
```sh
# Navigate to your development folder
cd ~/Projects/Pomologie/

# Create a new folder
mkdir PomologieML && cd PomologieML

# Initialize a virtual environment
python -m venv venv
source venv/bin/activate  # Mac/Linux
venv\Scripts\activate     # Windows

# Install dependencies
pip install flask opencv-python numpy
```
- **Flask:** Web framework for API  
- **OpenCV & NumPy:** Image processing libraries  

---

### **📌 Step 2: Create Flask ML API**
Create `app.py`:
```python
from flask import Flask, request, jsonify
import cv2
import numpy as np

app = Flask(__name__)

def detect_apples(image):
    # Placeholder ML logic (Replace with real model)
    nb_apfel = 10  # Dummy apple count
    confidence_score = 0.85  # Dummy confidence score
    return nb_apfel, confidence_score

@app.route('/process-image', methods=['POST'])
def process_image():
    file = request.files['image']
    img_array = np.asarray(bytearray(file.read()), dtype=np.uint8)
    img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

    # Run ML model
    nb_apfel, confidence_score = detect_apples(img)

    return jsonify({"nb_apfel": nb_apfel, "confidence_score": confidence_score})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001)
```
---

### **📌 Step 3: Run the ML API**
```sh
python app.py
```
🚀 **Now your ML API is running at `http://localhost:5001/process-image`!**

---

 

# Initialisation history
- This document is used for me to trace how i created this repository initialisation when all project was empty, for the next time i make a project. It serves only information purposes.

 
 in this doc we explain, the original setup of
| **Project**         | **Technology** | **Setup Command** |
|--------------------|--------------|----------------|
| **PomologieML** (ML API) | Flask + OpenCV | `python app.py` |

---
<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [Initialisation history](#initialisation-history)
  - [**2️⃣ Initializing PomologieML (ML API with Flask/FastAPI)**](#2-initializing-pomologieml-ml-api-with-flaskfastapi)
    - [**Step 1: Create PomologieML Project**](#step-1-create-pomologieml-project)
  - [**Project Structure**](#project-structure)
    - [**Step 2: Create Flask ML API**](#step-2-create-flask-ml-api)
    - [**Step 3: Run the ML API**](#step-3-run-the-ml-api)
<!-- TOC END -->
 
</details>

---

--- 
## **2️⃣ Initializing PomologieML (ML API with Flask/FastAPI)**
This will be your **machine learning API** that:  
✅ Receives images from Django 🖼️  
✅ Runs apple detection 🍏  
✅ Returns **number of apples (`nb_apfel`) & confidence score** 🤖  

---

### **Step 1: Create PomologieML Project**
```sh
# Navigate to your development folder
cd ~/Projects/Pomologie/

# Create a new folder
mkdir PomologieML && cd PomologieML

# Initialize a virtual environment
python3 -m venv venv
source venv/bin/activate  # Mac/Linux 

# Install dependencies
pip install flask opencv-python numpy requests
pip freeze > requirements.txt

```
- **Flask:** Web framework for API  
- **OpenCV & NumPy:** Image processing libraries  



## **Project Structure**
Touch files anbd create folder, after setup, your `PomoloBeeML/` directory should look like this:

```
PomoloBeeML/
│── venv/                  # Virtual environment
│── app.py                 # Main Flask app
│── requirements.txt        # List of dependencies
│── config.py               # Configuration settings
│── static/                 # Optional: Store static images
│── models/                 # Optional: Store ML models
│── uploads/                # Store uploaded images
```

--- 
---

### **Step 2: Create Flask ML API**
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

### **Step 3: Run the ML API**
```sh
python app.py
```
🚀 **Now your ML API is running at `http://localhost:5001/process-image`!**

---

 

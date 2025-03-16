# Initialisation history 
- This document is used for me to trace how i created this repository initialisation when all project was empty, for the next time i make a project. It serves only information purposes.

 

You're setting up **three development projects**:  
✔ **PomologieDjango** (Django Backend)  
✔ **PomologieML** (ML API with Flask/FastAPI)  
✔ **PomologieApp** (Android Studio)    

| **Project**         | **Technology** | **Setup Command** |
|--------------------|--------------|----------------|
| **PomologieApp** (Android) | Android Studio | (replicate an existing project created with android studio) |
| **PomologieDjango** (Backend) | Django + DRF | `django-admin startproject PomologieDjango` |
| **PomologieML** (ML API) | Flask + OpenCV | `python app.py` |

---

## **1️⃣ Initializing PomologieDjango (Django Backend)**
This will be your **backend API** handling:  
✅ Image storage 📂  
✅ ML result processing 🤖  
✅ Data sync with the app 🔄  

### **📌 Step 1: Create Django Project**
Run these commands in your terminal:
```sh
# Navigate to your development folder
cd ~/Projects/Pomologie/

# Create Django project
django-admin startproject PomologieDjango

# Move into project directory
cd PomologieDjango

# Create an app for core functionality
python manage.py startapp core
```

📌 **Now your project structure looks like this:**
```
PomologieDjango/
 ├── manage.py
 ├── PomologieDjango/    # Django project settings
 ├── core/               # Your main backend app
```

---

### **📌 Step 2: Install Required Packages**
Install necessary Python dependencies:
```sh
pip install django djangorestframework pillow requests
```
- **Django:** Main web framework  
- **Django REST Framework (DRF):** API support  
- **Pillow:** Image handling  
- **Requests:** For calling ML API  

---

### **📌 Step 3: Configure Django Settings**
Modify `PomologieDjango/settings.py`:
```python
INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'rest_framework',  # Add Django REST Framework
    'core',  # Your main app
]

# Media files (for image storage)
MEDIA_URL = '/media/'
MEDIA_ROOT = BASE_DIR / 'media'
```
Then, run:
```sh
# Create media folder for storing images
mkdir media
```

---

### **📌 Step 4: Set Up Database & Migrations**
```sh
python manage.py migrate
python manage.py createsuperuser
python manage.py runserver
```
🚀 **Your Django backend is now initialized!**

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

 


## **3 Initializing PomologieApp (Android frontend)**

duplicate the code of nathabee/extraction-app/visubee project using Android Studio :

To **duplicate an Android Studio project** and rename it from **VisuBee** to **PomoloBeeApp**, follow these **step-by-step instructions**:

---

## **1️⃣ Copy the Project to the New Location**
Run this command in your terminal:
```sh
cp -r /home/nathalie/coding/extraction-app/VisuBee /home/nathalie/coding/PomoloBee/PomoloBeeApp
```
This **copies** the entire **VisuBee** project to the new **PomoloBeeApp** location.

---

## **2️⃣ Rename the Project Inside Android Studio**
Now, **open Android Studio** and follow these steps:

### **📌 Step 1: Open the New Project**
- Open **Android Studio**.
- Select **"Open"** and navigate to:  
  📂 `/home/nathalie/coding/PomoloBee/PomoloBeeApp`
- Click **"Open"**.

---

### **📌 Step 2: Rename Project in Android Studio**
1. **Go to** `File` → `Refactor` → `Rename` → `Rename Project`
2. Change `VisuBee` → **PomoloBeeApp**
3. Click **"Refactor"** and confirm.

---

### **📌 Step 3: Rename the Project Folder**
- Go to `File` → `Close Project`
- Open your terminal and rename the project folder manually:
```sh
mv /home/nathalie/coding/PomoloBee/PomoloBeeApp/VisuBee /home/nathalie/coding/PomoloBee/PomoloBeeApp/PomoloBeeApp
```

---

### **📌 Step 4: Update `settings.gradle`**
Open `settings.gradle` inside the project and **update the root project name**:
```gradle
rootProject.name = "PomoloBeeApp"
```

---

### **📌 Step 5: Rename Package Name**
1. Open `AndroidManifest.xml`
2. Look for:
   ```xml
   package="com.example.visubee"
   ```
3. Change it to:
   ```xml
   package="com.example.pomolobee"
   ```

4. Go to `app/src/main/java/com/example/visubee/`
5. **Right-click** on `visubee` → `Refactor` → `Rename` → **Change to `pomolobee`**
6. Click **"Do Refactor"**.

---

### **📌 Step 6: Invalidate Cache & Restart**
- Go to `File` → `Invalidate Caches / Restart` → **Invalidate and Restart**.

---

### **📌 Step 7: Run the Project**
```sh
cd /home/nathalie/coding/PomoloBee/PomoloBeeApp
./gradlew clean
./gradlew build
```
- Open **Android Studio**.
- Run the project (`Shift` + `F10`).

🚀 **Done! Your project is successfully duplicated & renamed to PomoloBeeApp!** 🎉

Let me know if you need further refinements! 🚀
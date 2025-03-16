# Initialisation history 
- This document is used for me to trace how i created this repository initialisation when all project was empty, for the next time i make a project. It serves only information purposes.

---
## Table of Content
<!-- TOC -->
- [Initialisation history ](#initialisation-history)
  - [Table of Content](#table-of-content)
  - [**1️⃣ Initializing PomologieDjango (Django Backend)**](#1-initializing-pomologiedjango-django-backend)
    - [**📌 Step 1: Create Django Project**](#step-1-create-django-project)
    - [**📌 Step 2: Install Required Packages**](#step-2-install-required-packages)
    - [**📌 Step 3: Configure Django Settings**](#step-3-configure-django-settings)
    - [**📌 Step 4: Set Up Database & Migrations**](#step-4-set-up-database--migrations)
<!-- TOC END -->

---

  

 in this doc we explain, the original setup of
| **Project**         | **Technology** | **Setup Command** |
|--------------------|--------------|----------------| 
| **PomologieDjango** (Backend) | Django + DRF | `django-admin startproject PomologieDjango` | 

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

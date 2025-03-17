# Initialisation history 
- This document is used for me to trace how i created this repository initialisation when all project was empty, for the next time i make a project. It serves only information purposes.

---
## Table of Content
<!-- TOC -->
- [Initialisation history ](#initialisation-history)
  - [Table of Content](#table-of-content)
  - [**Prerequise**](#prerequise)
    - [✅ **1. Install Django**](#1-install-django)
    - [**Create Django Project**](#create-django-project)
    - [✅ **2. Set Up a Virtual Environment (Recommended)**](#2-set-up-a-virtual-environment-recommended)
    - [✅ **3 . add venv to .gitignore**](#3--add-venv-to-gitignore)
  - [Initialise Django project](#initialise-django-project)
    - [**📌 Step 1: Create Django Project**](#step-1-create-django-project)
    - [**📌 Step 2: Install Required Packages**](#step-2-install-required-packages)
    - [**📌 Step 3: Configure Django Settings**](#step-3-configure-django-settings)
    - [**📌 Step 4: Set Up Database & Migrations**](#step-4-set-up-database--migrations)
    - [we created the equirements.txt](#we-created-the-equirementstxt)
    - [**Why Did You Create a Superuser After Setting Up PostgreSQL?**](#why-did-you-create-a-superuser-after-setting-up-postgresql)
<!-- TOC END -->

---

  

 in this doc we explain, the original setup of
| **Project**         | **Technology** | **Setup Command** |
|--------------------|--------------|----------------| 
| **PomoloBeeDjango** (Backend) | Django + DRF | `django-admin startproject PomoloBeeDjango` | 

This will be your **backend API** handling:  
✅ Image storage 📂  
✅ ML result processing 🤖  
✅ Data sync with the app 🔄  

---

## **Prerequise**


### ✅ **1. Install Django**
Run the following command to install Django:
```bash
sudo apt install python3-django
```

📌 **Check if Django is installed** after installation:
```bash
django-admin --version
```
If it returns a version number, Django is installed successfully.

### **Create Django Project**
Run these commands in your terminal:
```sh
# Navigate to your development folder
cd ~/PomoloBee/

# Create Django project
django-admin startproject PomoloBeeDjango
```
---

### ✅ **2. Set Up a Virtual Environment (Recommended)**
It's best to use a virtual environment to manage dependencies.

1. **Navigate to your django project root**:
   ```bash
   cd  PomoloBee/PomoloBeeDjango
   ```
2. **Create a virtual environment**:
   ```bash
   python3 -m venv venv
   ```
3. **Activate the virtual environment**:
   ```bash
   source venv/bin/activate
   ```
4. **Install Django inside the virtual environment**:
   ```bash
   pip install django
   ```

---

### ✅ **3 . add venv to .gitignore**
After running the command,  **inside** the `PomoloBee` directory:
```bash
echo "PomoloBeeDjango/venv/" >> .gitignore
```
 
 ## ✅ **4 . database creation

install database : see **Django PostgreSQL specification** [Django_PostgreSQL](documentation/Django_PostgreSQL.md)  
 



## Initialise Django project

### **📌 Step 1: Create Django Project**
Run these commands in your terminal:
```sh
# Navigate to your development folder
cd ~/PomoloBee/PomoloBeeDjango

# Create an app for core functionality
python manage.py startapp core
```

📌 **Now your project structure looks like this:**
```
PomoloBeeDjango/
 ├── manage.py
 ├── PomoloBeeDjango/    # Django project settings
 ├── core/               # Your main backend app
 ├── venv/               # Python env
```

---

### **📌 Step 2: Install Required Packages**
Install necessary Python dependencies:
```sh
pip install django djangorestframework pillow requests  python-dotenv psycopg2-binary numpy
pip freeze > requirements.txt


```
- **Django:** Main web framework  
- **Django REST Framework (DRF):** API support  
- **Pillow:** Image handling  
- **Requests:** For calling ML API  

---

### **📌 Step 3: Configure Django Settings**


Modify the PomoloBeeDjango/settings.py 
 
create a .venv with settings replace with value

   ```bash
  SECRET_KEY=your-very-secret-key
  DEBUG=True

  # Database Configuration
  DATABASE_NAME=pomolobee
  DATABASE_USER=pomolo_user
  DATABASE_PASSWORD=your-secure-db-password
  DATABASE_HOST=localhost
  DATABASE_PORT=5432

   ```


Modify `PomoloBeeDjango/settings.py`:
```python

# Load environment variables from .env 
load_dotenv(os.path.join(BASE_DIR, '.env'))

# SECURITY WARNING: Keep the secret key used in production secret!
SECRET_KEY = os.getenv('SECRET_KEY')

# SECURITY WARNING: Don't run with debug turned on in production!
DEBUG = os.getenv('DEBUG', 'False') == 'True'

INSTALLED_APPS = [ 
  ...
    'rest_framework',  # Add Django REST Framework
    'core',  # Your main app
]

# Media files (for image storage)
MEDIA_URL = '/media/'
MEDIA_ROOT = BASE_DIR / 'media'

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql',
        'NAME': 'pomolobee',
        'USER': 'pomolo_user',
        'PASSWORD': 'secure_password',  # Use the password you set
        'HOST': 'localhost',
        'PORT': '5432',
    }
}



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
# i will put pomobee, this is the superuser of django admin
python manage.py runserver
```


### we created the equirements.txt
in the PomoloBee folder :
```sh
pip freeze > requirements.txt
```


🚀 **Your Django backend is now initialized!**

---


### **Why Did You Create a Superuser After Setting Up PostgreSQL?**
 
| **User Type**  | **Name** | **Purpose** |
|--------------|----------|------------|
| **PostgreSQL Database User** | `pomolo_user` | Used by Django to connect to the PostgreSQL database. |
| **Django Superuser** | `pomobee` | Allows you to log into `http://127.0.0.1:8000/admin/` and manage data from the admin panel. |

---
  
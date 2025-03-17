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
  - [Init Data Modele](#init-data-modele)
    - [test initialisation data in table with admin console user wuth django superuser pomobee](#test-initialisation-data-in-table-with-admin-console-user-wuth-django-superuser-pomobee)
    - [test initialisation](#test-initialisation)
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
│── core/ # Your main backend app
│   ├── migrations/
│   ├── __init__.py
│   ├── admin.py
│   ├── apps.py
│   ├── models.py   <--- Move your models here
│   ├── views.py
│   ├── urls.py
│   ├── tests.py
│── PomoloBeeDjango/    # Django project settings
│   ├── __init__.py
│   ├── settings.py  <--- Add 'core' to INSTALLED_APPS
│   ├── urls.py
│   ├── wsgi.py
│── manage.py
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

## Init Data Modele

Modify the PomoloBeeDjango/core/models.py to add modele defined in Django_Datamodele.md specification
Customize the admin panel for better display in  PomoloBeeDjango/core/admin.py

  ```bash
  cd  PomoloBee/PomoloBeeDjango 
  source venv/bin/activate 
  python manage.py makemigrations core
  python manage.py migrate
  python manage.py runserver

  ```

### test initialisation data in table with admin console user wuth django superuser pomobee

in your webbrowser log with the superuser  pomobee to  http://127.0.0.1:8000/admin/
The first time we create the initial configuration with the admin. 

Then we extract the configuration in case a new installation.

### test initialisation

  ```bash
psql -U pomolo_user -d pomolobee -h localhost -W
# for info to list table : SELECT schemaname, tablename FROM pg_tables ;
INSERT INTO core_field (short_name, name, description, orientation)
VALUES 
    ('C1', 'Maison', 'Champ situé au bord de la maison, humide.', 'NW'),
    ('C2', 'ChampSud', 'Champ situé au sud de la propriété, très ensoleillé.', 'S');


INSERT INTO core_fruit (short_name, name, description, yield_start_date, yield_end_date, yield_avg_kg, fruit_avg_kg)
VALUES 
    ('Swing_CG1', 'Cultivar Swing on CG1', 'Late harvest, sweet, crisp texture, medium storage (3-4 months), aromatic', '2025-09-15', '2025-10-05', 40, 0.2),
    ('Ladina_CG1', 'Cultivar Ladina on CG1', 'Mid-late harvest, balanced sweetness and acidity, long storage (5-6 months), juicy', '2025-09-25', '2025-10-15', 35, 0.22),
     ;

  ```
 
 ### test automatic initialisation with fixture

make initialisation of table in json files core/fixtures/initial_fields.json and core/fixtures/initial_fruit.json with the data you need

load the data
  ```bash
 python manage.py loaddata core/fixtures/initial_fields.json
 python manage.py loaddata core/fixtures/initial_fruits.json
 python manage.py loaddata core/fixtures/initial_raws.json

  ```

  ```bash
# check :
python manage.py shell

from core.models import Field
Field.objects.all()


from core.models import Fruit
Fruit.objects.all()
  ```




### **Why Did You Create a Superuser After Setting Up PostgreSQL?**
 
| **User Type**  | **Name** | **Purpose** |
|--------------|----------|------------|
| **PostgreSQL Database User** | `pomolo_user` | Used by Django to connect to the PostgreSQL database. |
| **Django Superuser** | `pomobee` | Allows you to log into `http://127.0.0.1:8000/admin/` and manage data from the admin panel. |

---
  
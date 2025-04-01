# Initialisation history
- This document is used for me to trace how i created this repository initialisation when all project was empty, for the next time i make a project. It serves only information purposes.

---
<details>
<summary>Table of Content</summary>
  
<!-- TOC -->
- [Initialisation history](#initialisation-history)
  - [**Prerequise**](#prerequise)
    - [Install Django*](#install-django)
    - [**Create Django Project**](#create-django-project)
    - [Set Up a Virtual Environment Recommended](#set-up-a-virtual-environment-recommended)
    - [add venv to .gitignore](#add-venv-to-gitignore)
  - [Initialise Django project](#initialise-django-project)
    - [Step 1 Create Django Project](#step-1-create-django-project)
    - [Step 2 Install Required Packages](#step-2-install-required-packages)
    - [Step 3 Configure Django Settings](#step-3-configure-django-settings)
    - [we created the requirements.txt](#we-created-the-requirementstxt)
  - [Init Data Modele](#init-data-modele)
    - [Why Did You Create a Superuser After Setting Up PostgreSQL?](#why-did-you-create-a-superuser-after-setting-up-postgresql)
    - [test initialisation data in table with admin console user wuth django superuser pomobee](#test-initialisation-data-in-table-with-admin-console-user-wuth-django-superuser-pomobee)
    - [test initialisation](#test-initialisation)
  - [Init Django code](#init-django-code)
  - [Test](#test)
    - [**run the test**](#run-the-test)
<!-- TOC END -->

</details>

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


### Install Django*
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

### Set Up a Virtual Environment Recommended
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

### add venv to .gitignore
After running the command,  **inside** the `PomoloBee` directory:
```bash
echo "PomoloBeeDjango/venv/" >> .gitignore
```
 
 ## ✅ **4 . database creation

install database : see **Django PostgreSQL specification** [Django_PostgreSQL](docs/Django_PostgreSQL.md)  
 



## Initialise Django project

### Step 1 Create Django Project
Run these commands in your terminal:
```sh
# Navigate to your development folder
cd ~/PomoloBee/PomoloBeeDjango

# Create an app for core functionality
python manage.py startapp core
```

- Now your project structure looks like this:
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

### Step 2 Install Required Packages
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

### Step 3 Configure Django Settings


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

# SECURITY WARNING Keep the secret key used in production secret!
SECRET_KEY = os.getenv('SECRET_KEY')

# SECURITY WARNING Don't run with debug turned on in production!
DEBUG = os.getenv('DEBUG', 'False') == 'True'

INSTALLED_APPS = [ 
  ...
    'rest_framework',  # Add Django REST Framework
    'core',  # Your main app
]

# Media files for image storage
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
 



### we created the requirements.txt
in the PomoloBee folder :
```sh
pip freeze > requirements.txt
```

## Init Data Modele

Modify the PomoloBeeDjango/core/models.py to ad necessary models
Customize the admin panel for better display in  PomoloBeeDjango/core/admin.py

  ```bash
  cd  PomoloBee/PomoloBeeDjango 
  source venv/bin/activate 
  python manage.py makemigrations core
  python manage.py migrate
  python manage.py migrate 
  python manage.py loaddata core/fixtures/initial_superuser.json
  python manage.py shell

   ```

  - createsuperuser should be "pomobee" this is the superuser of django admin

```python 
from django.contrib.auth.models import User
u = User.objects.get(username='pomobee')
u.set_password('new_secure_password')
u.save()
```
 

### Why Did You Create a Superuser After Setting Up PostgreSQL?
 
| **User Type**  | **Name** | **Purpose** |
|--------------|----------|------------|
| **PostgreSQL Database User** | `pomolo_user` | Used by Django to connect to the PostgreSQL database. |
| **Django Superuser** | `pomobee` | Allows you to log into `http://127.0.0.1:8000/admin/` and manage data from the admin panel. |

---
  
  
### test initialisation data in table with admin console user wuth django superuser pomobee

in your webbrowser log with the superuser  pomobee to  http://127.0.0.1:8000/admin/
The first time we create the initial configuration with the admin. 

Then we extract the configuration in case a new installation.

### test initialisation

  ```bash
psql -U pomolo_user -d pomolobee -h localhost -W
# for info to list table SELECT schemaname tablename FROM pg_tables ;
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

make initialisation of table in json files  with the data you need

load the data
  ```bash
 python manage.py loaddata core/fixtures/initial_farms.json
 python manage.py loaddata core/fixtures/initial_fields.json
 python manage.py loaddata core/fixtures/initial_fruits.json
 python manage.py loaddata core/fixtures/initial_rows.json

  ```

  ```bash
# check
python manage.py shell

from core.models import Field
Field.objects.all()


from core.models import Fruit
Fruit.objects.all()
  ```



## Init Django code

We create an empty shell having correct endpoint and dealing with the correct data and dataformat :
- note that the complexe behaviour like ML interaction, logic and error management will be introduce later in the code

  -  installed and configured  drf-spectacular (pip install, settings.py.REST_FRAMEWORK,urls.py)
  -  create PomoloBeeDjango/core/urls.py with all acess point mentionned in the spec  [specification API](API.md)
  -  add a reference to it in PomoloBeeDjango/PomoloBeeDjango/urls.py path('api/', include('core.urls')), 
 

      To simplify this step, you can feed chatgpt with the API.md endpoint table


    - serializers.py and views.py are also initialized from the API.md document
    Feed Chatgpt **Step by step** with part of the endpoint specification API.md, to be check coherency of teh new views and serilializer.

-from django.conf import settings
from django.conf.urls.static import static

urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

## Test


  ```bash
  pip install pytest pytest-django requests pillow  
  
  touch core/tests.py 
  # add the test there
  ``` 
  **Add permission**
  PostgreSQL user needs  CREATE DATABASE permissions:
  # psql -U pomolo_user -d pomolobee -h localhost -W
  sudo -u postgres psql
  ALTER ROLE pomolo_user CREATEDB;

### **run the test**

  ```bash
  #### to run all together
  python manage.py test core.tests

  #### to run separate
  python manage.py test core.tests.test_migration
  python manage.py test core.tests.test_endpoint
  python manage.py test core.tests.test_workflow 
  ...etc

  ```  

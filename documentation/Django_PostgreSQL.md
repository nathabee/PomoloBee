# **Install PostgreSQL**  


### 1️⃣ **Ensure PostgreSQL is Installed** 
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

### 2️⃣ **Create a Database & User for PomoloBee**
```bash
sudo -u postgres psql
```
Inside PostgreSQL prompt:
```sql
CREATE DATABASE pomolobee;
CREATE USER pomolo_user WITH ENCRYPTED PASSWORD 'my_super_password';
GRANT ALL PRIVILEGES ON DATABASE pomolobee TO pomolo_user;
ALTER DATABASE pomolobee OWNER TO pomolo_user;
```
Exit PostgreSQL:
```sql
\q
```

### 3️⃣ **Update Django `settings.py`**
Modify your database settings:
```python
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql',
        'NAME': 'pomolobee',
        'USER': 'pomolo_user',
        'PASSWORD': 'my_super_password',
        'HOST': 'localhost',
        'PORT': '5432',
    }
}
```
### 4️⃣ **Apply Migrations**
```bash
python manage.py migrate
```

---

## **📌 PHASE4 : Future-Proofing for Geospatial Data**
If you decide to use **PostGIS** (for GPS tracking, orchard mapping, etc.), enable it in PostgreSQL:
```bash
sudo -u postgres psql -d pomolobee -c "CREATE EXTENSION postgis;"
```
This allows storing **GPS coordinates, maps, and spatial queries** in the future.  

---

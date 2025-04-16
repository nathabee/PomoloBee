# Django Specification
---
<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [Django Specification](#django-specification)
  - [API-to-DB Impact Table](#api-to-db-impact-table)
  - [Summary Logic Chain If Working](#summary-logic-chain-if-working)
  - [Trigger Behaviour](#trigger-behaviour)
    - [Methodologie to implement a triggered behaviour](#methodologie-to-implement-a-triggered-behaviour)
    - [Unit Test](#unit-test)
    - [list of trigger](#list-of-trigger)
    - [table](#table)
  - [Data model](#data-model)
    - [**Phase 1 MVP Model Structure**](#phase-1-mvp-model-structure)
    - [**Phase 2 User Authentication Access Control**](#phase-2-user-authentication-access-control)
<!-- TOC END -->
 
</details>
---
 


## API-to-DB Impact Table


| Step | API Endpoint | Method | View | DB Impact | Consequence After Request | ➡️ Triggers Step |
|------|--------------|--------|------|-----------|----------------------------|------------------|
| 1️⃣ | `/api/images/` | POST | `ImageView.post()` | ✅ Creates `ImageHistory`<br>🖼️ Saves image to storage | 🔁 Sends image to ML (`/process-image/`) | ⏩ Step 2️⃣ |
| 2️⃣ | `/api/images/<id>/ml_result/` | POST | `MLResultView.post()` | 🔄 Updates `ImageHistory`:<br>• `nb_fruit`, `confidence_score`, `processed = True` | 🧠 Triggers signal: `post_save(ImageHistory)` | ⏩ Step 3️⃣ |
| 3️⃣ | *(Signal)* | — | `post_save` in `signals.py` | ✅ Creates:<br>• `HistoryRow`<br>• `HistoryEstimation` | 💾 Saves estimation (calculated from row & fruit) | ⏩ Step 4️⃣ |
| 4️⃣ | `/api/estimations/<id>/` | GET | `EstimationView.get()` | ❌ No DB write | 📤 Returns `fruit_plant`, `plant_kg`, `row_kg`, `confidence_score` | 🔚 Final user-visible result |
| 5️⃣ | `/api/retry_processing/` | POST | `RetryProcessingView` | ❌ No DB write | 🔁 Re-sends existing image to ML | ⏩ Step 2️⃣ again |
| 6️⃣ | `/api/images/<id>/` | DELETE | `ImageDeleteView` | 🗑 Deletes `ImageHistory`<br>🖼 Deletes file from storage | ⚠️ History data not deleted | 🔚 Clean-up |
| 7️⃣ | `/api/images/<id>/status/` | GET | `ImageStatusView` | ❌ No DB write | 📤 Returns `processed: true/false` | 🔚 Polling mechanism |
| 8️⃣ | `/api/fields/`, `/api/fruits/`, `/api/locations/` | GET | `FieldViewSet`, etc. | ❌ No DB write | 📤 Returns static data | 🔚 App init |
| 9️⃣ | `/api/ml/version/` | GET | `MLVersionView.get()` | ❌ No DB write | ✅ Confirms ML is online (or not) | 🔚 Dev check only |

---


---

## Summary Logic Chain If Working

```
App POST /api/images/ 
→ Django creates ImageHistory & sends to ML 
→ ML POST /ml_result/ 
→ Django updates ImageHistory & creates HistoryRow/Estimation 
→ App GET /estimations/<id> 
→ Displays results
```

 
## Trigger Behaviour



### Methodologie to implement a triggered behaviour
    ```bash
    - create trigger in : 📁 File: core/signals.py 
    - add  in core/apps.py : import core.signals   
    - add in the settings APP_INSTALLED : 'core.apps.CoreConfig', remove core
    - add in core/__init__.py :  default_app_config = 'core.apps.CoreConfig'
    - after change :    python manage.py makemigrations
                        python manage.py migrate
    ```

### Unit Test
add a log in core/signals.py:
@receiver(post_save, sender=ImageHistory)
def image_processed_handler(sender, instance, created, **kwargs):
    print(f"🔔 Signal fired! Image ID: {instance.id}, Processed: {instance.processed}")


python manage.py shell
```python 
from core.models import ImageHistory, Row
row = Row.objects.first()
img = ImageHistory.objects.create(image_path="demo.jpg", row=row, processed=True)
```

### list of trigger
post_save signal for ImageHistory| This will auto-trigger the creation of HistoryRow and HistoryEstimation when ML results (processed=True) are saved.




### table
 



## Data model

### **Phase 1 MVP Model Structure**

| Description                                  | Django Model                  |
|----------------------------------------------|-------------------------------|
| Agricultural field                           | `Field(models.Model)`         |
| Tree row (within a field)                    | `Row(models.Model)`           |
| Fruit type (linked to row)                   | `Fruit(models.Model)`         |
| Image storage for estimation analysis        | `ImageHistory(models.Model)`  |
| History of row analysis (processed results)  | `HistoryRow(models.Model)`    |
| Yield estimation summary                     | `HistoryEstimation(models.Model)` |
| Farm owned by user                           | `Farm(models.Model)`       |

> 🔸 **Note:**  
Although a `Farm` concept exists, the access by farm ID **is not implemented** or used in Phase 1.

---

### **Phase 2 User Authentication Access Control**

#### **New Models Ownership Rules**

| Description               | Django Model               |
|---------------------------|----------------------------|
| Registered user (default) | `User` (from `auth.models`)|

---

#### **Access Control Strategy**

- Each **User** owns one or multiple **Farms**
- Each **Farm** contains multiple **Fields**
- Each **Field** contains multiple **Rows (tree rows)**

---

#### **Authorization Rules**

| Operation                             | Permitted if...                                    |
|---------------------------------------|----------------------------------------------------|
| View fields / rows                    | Field belongs to a farm owned by the user          |
| Upload image for row                  | Row belongs to user's farm                         |
| View image or yield estimation result | Image is tied to a row in one of the user’s farms  |

---
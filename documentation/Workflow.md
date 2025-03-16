# **📜 Pomolobee Workflow Document**  

## **Table of Contents**  
<!-- TOC -->
- [**📜 Pomolobee Workflow Document**  ](#pomolobee-workflow-document)
  - [**Table of Contents**  ](#table-of-contents)
  - [Diagramme](#diagramme)
  - [**🌍 Data Flow**](#data-flow)
  - [**1. Workflow Summary**  ](#1-workflow-summary)
    - [**📌 Case: App Initializes Data**  ](#case-app-initializes-data)
    - [**📌 Case: App Requests Estimation Based on a Picture**  ](#case-app-requests-estimation-based-on-a-picture)
    - [**📌 Case: App Displays Data**  ](#case-app-displays-data)
  - [**1️⃣ API DJANGO -> APP**  ](#1-api-django--app)
  - [**2️⃣ API DJANGO <-> ML**  ](#2-api-django--ml)
  - [**3️⃣ Process Image API (Django to ML)**  ](#3-process-image-api-django-to-ml)
  - [**2. Detailed Requirements**  ](#2-detailed-requirements)
    - [**App Requirements**  ](#app-requirements)
    - [**ML Model Requirements**  ](#ml-model-requirements)
    - [**Django Backend Requirements**  ](#django-backend-requirements)
  - [**3. Explanation of Calculation**  ](#3-explanation-of-calculation)
    - [**📌 How Yield is Estimated**  ](#how-yield-is-estimated)
  - [**4. API Design**  ](#4-api-design)
    - [**API Call Order**  ](#api-call-order)
    - [**Polling Strategy**  ](#polling-strategy)
  - [**Error Handling Strategy**  ](#error-handling-strategy)
<!-- TOC END -->


---
## Diagramme
## **🌍 Data Flow**
```mermaid
graph TD
  subgraph App
    MobileApp["📱 Pomolobee App"]
  end

  subgraph Backend
    DjangoServer["🌐 Django Backend"]
    Database["📂 PostgreSQL Database"]
  end

  subgraph ML System
    MLService["🖥️ ML Model (Flask/FastAPI)"]
  end

  subgraph Storage
    FileSystem["🖼️ Image Storage"]
  end

  MobileApp -- "📍 Fetch Available Fields & Raws" --> DjangoServer
  DjangoServer -- "📄 Provide Field & Raw Data" --> MobileApp
  MobileApp -- "📤 Upload Image & Raw ID" --> DjangoServer
  DjangoServer -- "📂 Save Image" --> FileSystem
  DjangoServer -- "🔄 Send Image to ML" --> MLService
  MLService -- "🔢 Detect Apples & Confidence Score" --> DjangoServer
  DjangoServer -- "📄 Update Image History & Store Results" --> Database
  MobileApp -- "📥 Fetch All Uploaded Images" --> DjangoServer
  MobileApp -- "📥 Check Processing Status" --> DjangoServer
  DjangoServer -- "📄 Return Status (Done/In Progress/Failed)" --> MobileApp
  MobileApp -- "📥 Fetch Estimation Results" --> DjangoServer
  DjangoServer -- "📄 Provide Yield Data & Save to History" --> MobileApp
  MobileApp -- "📥 Fetch Estimation History" --> DjangoServer
```

## **1. Workflow Summary**  

### **📌 Case: App Initializes Data**  
1. **App starts up or the user refreshes data.**  
2. **App fetches static data** from Django:  
   - `Field`, `Raw`, `Fruit` tables.  
   - This data is stored locally for offline use.  
3. **App syncs periodically** to check for updates in Django.  

---

### **📌 Case: App Requests Estimation Based on a Picture**  

#### **Step 1: App Uploads Image**  
📌 `POST /api/images/`  
📩 **App sends:** `image`, `raw_id`, `date`  
📩 **Backend returns:** `image_id`  

**Django stores image & schedules ML processing:**  
   - Saves image in the **file system** on the server.  
   - Creates a new entry in `ImageHistory`:  
     - `image_path`: Path to the saved image.  
     - `nb_apfel`: Placeholder (waiting for ML result).  

---

#### **Step 2: ML Processes Image (Async Job)**  
📌 **ML model detects `nb_apfel` and updates `ImageHistory`.**  
📌 **ML also returns `confidence_score`.**  
📌 **Django updates `ImageHistory` and creates `HistoryEstimation`.**  

---

#### **Step 3: Retrieve Estimation**  
📌 `GET /api/estimations/{image_id}`  
📩 **App requests:** `image_id`  
📩 **Backend returns:**  
```json
{
    "plant_apfel": 12,
    "plant_kg": 2.4,
    "raw_kg": 48.0,
    "confidence_score": 0.85,
    "status": "done"
}
```
📌 **Django creates `HistoryRaw` and `HistoryEstimation`, referencing `ImageHistory`**:  
   - **ML results from `ImageHistory`.**  
   - **Calculated yield per plant (`plant_kg`).**  
   - **Total estimated yield (`raw_kg`).**  
   - **References the saved image (`id_image`).**  

---

### **📌 Case: App Displays Data**  
1. **Displays static data (Fields, Raws, Fruits)**  
   - Retrieved from Django and stored locally.  

2. **Displays estimation results**  
   - Fetches the latest `HistoryRaw` entries for the selected `Raw`.  
   - Shows `plant_apfel`, `plant_kg`, `raw_kg`, and other ML estimations.  

---

## **1️⃣ API DJANGO -> APP**  
**Endpoints for communication between Django and the App:**  
- `POST /api/images/` → Uploads an image & starts ML processing  
- `GET /api/images/{image_id}/status` → Checks if ML has processed the image  
- `GET /api/estimations/{image_id}` → Retrieves the estimation results  

| `GET /api/fields/` | fetch all fields |
| `GET /api/fields/{field_id}/raws/` |to fetch raws for a given field | 


---

## **2️⃣ API DJANGO <-> ML**  
**Endpoints for communication between Django and the ML model:**  
- `POST /process-image/` → Django sends an image to ML for processing  
- ML returns: `nb_apfel` (number of apples detected) and `confidence_score`  

---

## **3️⃣ Process Image API (Django to ML)**  
📌 **Step 1: App uploads image** → `POST /api/images/`  
📌 **Step 2: Django sends image to ML API** → `POST /process-image/`  
📌 **Step 3: ML detects apples & returns results**  

✅ **Django View: Sends Image to ML API**
 
---
## **2. Detailed Requirements**  

### **App Requirements**  
✅ Store static data locally for offline mode.  
✅ Send an image and raw_id to Django for estimation.  
✅ Fetch results (`HistoryRaw`) for past estimations.  
✅ Sync with Django when online.  

### **ML Model Requirements**  
✅ Process an image and return `nb_apfel` (number of apples detected).  
✅ Return results quickly to avoid app delays.  
✅ Be integrated with Django, either running inside Django or as an external service.  

### **Django Backend Requirements**  
✅ Store the image on the server file system.  
✅ Create `ImageHistory` with image path + ML results.  
✅ Calculate `plant_kg` and `raw_kg` before saving to `HistoryRaw`.  
✅ Provide API endpoints for the app to fetch data. 
✅ Store the image **on the local file system or a cloud storage solution (e.g., AWS S3, Google Cloud Storage)**.  


---

## **3. Explanation of Calculation**  

### **📌 How Yield is Estimated**  
1. **ML Model detects apples in the image**  
   - Frontend sends `image + raw ID + date` to Django.  
   - Django stores the image path in `ImageHistory`.  
   - ML analyzes the image and returns `nb_apfel` (number of apples detected).  

2. **Django calculates expected yield**  
   - **`plant_apfel = nb_apfel`** (ML-detected apples per plant).  
   - **`plant_kg = plant_apfel * fruit_avg_kg`** (expected weight per plant).  
   - **`raw_kg = plant_kg * raw.nb_plant`** (expected total weight for the raw).  

---

## **4. API Design**  

### **API Call Order**  
📌 `POST /api/images/` (Upload Image)  
📌 `GET /api/images/{image_id}/status` (Check Processing Status)  
📌 `GET /api/estimations/{image_id}` (Fetch Estimation Results)  

---

### **Polling Strategy**  
📌 The app checks `GET /api/images/{image_id}/status` every **minute**.  
📌 If `status = "done"`, the app fetches results.  
📌 If the process takes longer than **5 retries (5 minutes)**, the app should **show a warning**.  
📌 If ML takes longer than 5 minutes, Django should **log the delay** and optionally **send a retry request to ML**.  


🔹 **Why?**  
- Prevents infinite polling loops.  
- Ensures the user is **not left waiting indefinitely**.  

---

## **Error Handling Strategy**  
📌 **What if ML processing fails?**  
- If ML **returns an error**, Django should mark `processed = false` in `ImageHistory`.  
- The app should **stop polling after 5 attempts** and **display an error message**.

📌 **What if the app sends an invalid image?**  
- Django should return `400 Bad Request` if the image format is incorrect.  
- The app should prompt the user to upload a valid image.


--- 
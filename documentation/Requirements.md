# PomoloBee
 


## **Project Definition PomoloBee Bee Smart Know Your fruit**

### **Goal**
Develop an **Android app** (Kotlin + Android Studio) that allows farmers to estimate **fruit harvest yield** using AI-based **video or image analysis**. The system will use a **cloud-based backend (VPS)** to process data and provide accurate results.  

The **PomoloBee** app will now focus **only on image-based fruit yield estimation**.  
- **Video-based processing has been postponed to a future milestone**.  
- **Offline-first functionality is now a core feature**, allowing farmers to store images locally and manually sync data when online.  

---
<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [PomoloBee](#pomolobee)
  - [**Project Definition PomoloBee Bee Smart Know Your fruit**](#project-definition-pomolobee-bee-smart-know-your-fruit)
    - [**Goal**](#goal)
  - [**Features Functionalities**](#features-functionalities)
    - [**1 Mobile App Frontend Android**](#1-mobile-app-frontend-android)
    - [**2 Cloud Backend Django API**](#2-cloud-backend-django-api)
    - [**3 Machine Learning Model AI for fruit Detection**](#3-machine-learning-model-ai-for-fruit-detection)
    - [**4 Offline Mode Storage**](#4-offline-mode-storage)
  - [**Updated Data Flow**](#updated-data-flow)
  - [**Updated Milestones**](#updated-milestones)
    - [**Phase 1 MVP Current**](#phase-1-mvp-current)
    - [**Phase 2 AI Enhancements Manual Input Access Control**](#phase-2-ai-enhancements-manual-input-access-control)
    - [**Phase 3 Advanced Features Video Support**](#phase-3-advanced-features-video-support)
  - [**API Endpoint Mapping and Flow Diagram**](#api-endpoint-mapping-and-flow-diagram)
<!-- TOC END -->
  
</details>
 

---

## **Features Functionalities**
### **1 Mobile App Frontend Android**
📱 **User Actions:**  
✅ **Take a Picture** – User captures images of fruit trees for yield estimation.  
✅ **Store Images Offline** – Images are **stored locally first** before uploading.  
✅ **Select Orchard Location** – Farmers select field, raw, and tree count.  
✅ **Analyze Image (Local or Cloud)** – Farmers can **choose between local analysis or backend processing**.  
✅ **Receive Harvest Estimate** – Displays **fruit count, confidence score, and estimated yield**.  

🔧 **Tech Stack:**  
- **Language:** Kotlin  
- **Storage:** Jetpack DataStore for local image storage.  
- **Networking:** Retrofit (API calls to VPS).  
- **UI:** Jetpack Compose.  

---
  
### **2 Cloud Backend Django API**
🌐 **Server Responsibilities:**  
✅ **Receive image uploads from the app.**  
✅ **Detect fruit & count them using an AI model.**  
✅ **Return results to the app.**  
✅ **Allow reprocessing if AI results seem inaccurate.**  

🔧 **Tech Stack:**  
- **Backend Framework:** Django REST Framework.  
- **ML Processing:** OpenCV, YOLOv8.  
- **Storage:** PostgreSQL (for user data & image metadata).  
- **Hosting:** VPS.  

---
  
### **3 Machine Learning Model AI for fruit Detection**
🤖 **AI Tasks:**  
✅ **Detect fruit in Images** – Identify fruit using object detection.  
✅ **Estimate Yield** – Predict fruit count per tree row.  
❌ **(Postponed to Phase 2)** – fruit maturity classification (color-based).  

🔧 **Tech Stack:**  
- **Object Detection Model:** YOLOv8.  
- **AI Processing Mode:** Backend API (local AI optional).  

---
  
### **4 Offline Mode Storage**
✅ **Jetpack DataStore stores unsent images.**  
✅ **Manual sync instead of automatic upload.**  
✅ **Local AI model (optional) for offline estimation.**  

---
  
## **Updated Data Flow**
1️⃣ **User captures an image** (offline storage enabled).  
2️⃣ **User selects a field & raw** (manual input).  
3️⃣ **User uploads the image when online OR runs local AI analysis.**  
4️⃣ **AI detects fruit & estimates yield.**  
5️⃣ **Backend sends results back to the app.**  
6️⃣ **User reviews yield estimation & history.**  

---
  
 

## **Updated Milestones**

### **Phase 1 MVP Current**
- **Offline image storage & manual upload**  
- **Basic fruit detection model (YOLOv8)**  
- **Simple backend API (Django + PostgreSQL)**  
- **Basic processing screen to show estimation results**  

---

### **Phase 2 AI Enhancements Manual Input Access Control**
  

🔹 **Manual Override of AI Results**  
- Farmers can **adjust fruit count and yield** if AI detection seems inaccurate  
- New **"Manual Input Mode"** added to the **ResultScreen** (editable values)

🔹 **Updated Sync & Processing Workflow**  
- Farmers can choose between:
  - **Backend AI Processing (default)**
  - **Local-only mode : configuration can be loaded with json files locally**  
- New toggle in **SettingsScreen** to configure this behavior  

🔹 **Authentication and Access Control (NEW)**  
- 📱 **Android app**: Login screen using **standard credentials (username/password)**  
- 🔐 **Django backend**:
  - Use **Django’s built-in authentication** system (Token or Session auth)
  - Each `User` is associated with **one or more Farms**
  - Each `Field` belongs to a **Farm**
  - Users can **only view fields, raws, and data related to their farm(s)**
- 🌱 Enables secure **multi-user access** and **data separation per farm**


🔹 **User Access Model**
- Each user is tied to one or more **Farms**
- Users can:
  - View only their **fields**, **raws**, and **estimations**
  - Upload images only to **authorized rows**
- Auth method: **Token-based login**

---

### **Phase 3 Advanced Features Video Support**

🔹 **Historical Tracking & Yield Comparison**  
- View **past estimations** in a new **HistoryScreen**  
- Compare **AI predictions vs. actual harvest** recorded manually  

🔹 **Video-Based fruit Detection**  
- Record a **video walk-through** of a row instead of still photos  
- Use **Optical Flow Tracking (Lucas-Kanade or Farneback)** to track fruit frame-by-frame and **avoid duplicates**

🔹 **Export & Integration Tools**  
- Export reports as **CSV or PDF** for local analysis  
- API hooks for potential **integration with smart farming dashboards/tools**  

🔹 **fruit Maturity Classification**  
- Identify **ripe vs. unripe fruit** based on color (HSV analysis)  
- Useful for **harvest timing predictions**

 
## **API Endpoint Mapping and Flow Diagram**
📌 All API endpoints, request/response examples, and error codes are detailed in:

📖 [Full API Specification → API.md](API.md)

📌  Flow Diagram is detailed in:

📖 [Full API Specification → Workflow.md](Workflow.md)
 
 
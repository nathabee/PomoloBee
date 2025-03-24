# PomoloBee
 


## **Project Definition PomoloBee Bee Smart Know Your Apple**

### **Goal**
Develop an **Android app** (Kotlin + Android Studio) that allows farmers to estimate **apple harvest yield** using AI-based **video or image analysis**. The system will use a **cloud-based backend (VPS)** to process data and provide accurate results.  

The **PomoloBee** app will now focus **only on image-based apple yield estimation**.  
- **Video-based processing has been postponed to a future milestone**.  
- **Offline-first functionality is now a core feature**, allowing farmers to store images locally and manually sync data when online.  

---
<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [PomoloBee](#pomolobee)
  - [**Project Definition PomoloBee Bee Smart Know Your Apple**](#project-definition-pomolobee-bee-smart-know-your-apple)
    - [**Goal**](#goal)
  - [**Features Functionalities**](#features-functionalities)
    - [**1 Mobile App Frontend Android**](#1-mobile-app-frontend-android)
    - [**2 Cloud Backend Django API**](#2-cloud-backend-django-api)
    - [**3 Machine Learning Model AI for Apple Detection**](#3-machine-learning-model-ai-for-apple-detection)
    - [**4 Offline Mode Storage**](#4-offline-mode-storage)
  - [**Updated Data Flow**](#updated-data-flow)
  - [**Updated Milestones**](#updated-milestones)
  - [**Updated Milestones**](#updated-milestones)
    - [**Phase 1 MVP Current**](#phase-1-mvp-current)
    - [**Phase 2 AI Enhancements Manual Input Access Control**](#phase-2-ai-enhancements-manual-input-access-control)
    - [**Phase 3 Advanced Features Video Support**](#phase-3-advanced-features-video-support)
<!-- TOC END -->
  
</details>
 

---

## **Features Functionalities**
### **1 Mobile App Frontend Android**
📱 **User Actions:**  
✅ **Take a Picture** – User captures images of apple trees for yield estimation.  
✅ **Store Images Offline** – Images are **stored locally first** before uploading.  
✅ **Select Orchard Location** – Farmers select field, raw, and tree count.  
✅ **Analyze Image (Local or Cloud)** – Farmers can **choose between local analysis or backend processing**.  
✅ **Receive Harvest Estimate** – Displays **apple count, confidence score, and estimated yield**.  

🔧 **Tech Stack:**  
- **Language:** Kotlin  
- **Storage:** Jetpack DataStore for local image storage.  
- **Networking:** Retrofit (API calls to VPS).  
- **UI:** Jetpack Compose.  

---
  
### **2 Cloud Backend Django API**
🌐 **Server Responsibilities:**  
✅ **Receive image uploads from the app.**  
✅ **Detect apples & count them using an AI model.**  
✅ **Return results to the app.**  
✅ **Allow reprocessing if AI results seem inaccurate.**  

🔧 **Tech Stack:**  
- **Backend Framework:** Django REST Framework.  
- **ML Processing:** OpenCV, YOLOv8.  
- **Storage:** PostgreSQL (for user data & image metadata).  
- **Hosting:** VPS.  

---
  
### **3 Machine Learning Model AI for Apple Detection**
🤖 **AI Tasks:**  
✅ **Detect Apples in Images** – Identify apples using object detection.  
✅ **Estimate Yield** – Predict apple count per tree row.  
❌ **(Postponed to Phase 2)** – Apple maturity classification (color-based).  

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
4️⃣ **AI detects apples & estimates yield.**  
5️⃣ **Backend sends results back to the app.**  
6️⃣ **User reviews yield estimation & history.**  

---
  
## **Updated Milestones**

Great! Here's the updated **Milestones** section with your new **authentication and user-farm access control requirements** integrated into **Phase 2**.

---

## **Updated Milestones**

### **Phase 1 MVP Current**
- **Offline image storage & manual upload**  
- **Basic apple detection model (YOLOv8)**  
- **Simple backend API (Django + PostgreSQL)**  
- **Basic processing screen to show estimation results**  

---

### **Phase 2 AI Enhancements Manual Input Access Control**

🔹 **Local AI Model for Offline Estimation**  
- Farmers can analyze images **without internet** using an **on-device AI model** (OpenCV + TensorFlow Lite)  
- Enables instant feedback without needing backend processing  

🔹 **Manual Override of AI Results**  
- Farmers can **adjust apple count and yield** if AI detection seems inaccurate  
- New **"Manual Input Mode"** added to the **ResultScreen** (editable values)

🔹 **Updated Sync & Processing Workflow**  
- Farmers can choose between:
  - **Backend AI Processing (default)**
  - **Local-only processing mode**  
- New toggle in **SettingsScreen** to configure this behavior  

🔹 **Authentication and Access Control (NEW)**  
- 📱 **Android app**: Login screen using **standard credentials (username/password)**  
- 🔐 **Django backend**:
  - Use **Django’s built-in authentication** system (Token or Session auth)
  - Each `User` is associated with **one or more Farms**
  - Each `Field` belongs to a **Farm**
  - Users can **only view fields, raws, and data related to their farm(s)**
- 🌱 Enables secure **multi-user access** and **data separation per farm**

---

### **Phase 3 Advanced Features Video Support**

🔹 **Historical Tracking & Yield Comparison**  
- View **past estimations** in a new **HistoryScreen**  
- Compare **AI predictions vs. actual harvest** recorded manually  

🔹 **Video-Based Apple Detection**  
- Record a **video walk-through** of a row instead of still photos  
- Use **Optical Flow Tracking (Lucas-Kanade or Farneback)** to track apples frame-by-frame and **avoid duplicates**

🔹 **Export & Integration Tools**  
- Export reports as **CSV or PDF** for local analysis  
- API hooks for potential **integration with smart farming dashboards/tools**  

🔹 **Apple Maturity Classification**  
- Identify **ripe vs. unripe apples** based on color (HSV analysis)  
- Useful for **harvest timing predictions**

 
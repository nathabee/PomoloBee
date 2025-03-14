# PomoloBee
 

## **📌 Project Definition: PomoloBee – Bee Smart, Know Your Apple**

### **🔹 Goal:**  
Develop an **Android app** (Kotlin + Android Studio) that allows farmers to estimate **apple harvest yield** using AI-based **video or image analysis**. The system will use a **cloud-based backend (VPS)** to process data and provide accurate results.  

---

## **📍 Features & Functionalities**
### **1️⃣ Mobile App (Frontend – Android)**
📱 **User Actions:**  
✅ **Record or Upload Video** – User walks through the orchard while capturing video.  
✅ **Take a Picture** – Alternative to video for quick analysis.  
✅ **Mark Orchard Parameters** – Farmer defines start and end of a tree row (e.g., with red markers).  
✅ **Enter Field Data** – Total orchard row length, tree count, sample apple size.  
✅ **Receive Harvest Estimate** – Displays apple count and estimated yield.  

🔧 **Tech Stack:**  
- **Language:** Kotlin  
- **Networking:** Retrofit (API calls to VPS)  
- **UI:** Jetpack Compose or XML-based UI  

---

### **2️⃣ Cloud Backend (VPS – Django or Flask API)**
🌐 **Server Responsibilities:**  
✅ **Receive video/image uploads from the app**  
✅ **Extract key frames from video** (1 per second or as needed)  
✅ **Apple Detection & Counting (AI Model)**  
   - Detects apples in images  
   - Differentiates between growth stages (small green vs. ripe apples)  
   - Avoids duplicate counting using **Optical Flow Tracking**  
✅ **Calculate Total Yield Estimate**  
   - Uses detected apples per meter to scale up yield  
✅ **Return Results to the App**  

🔧 **Tech Stack:**  
- **Backend Framework:** Django REST Framework or Flask  
- **ML Processing:** OpenCV, YOLOv8, TensorFlow/PyTorch  
- **Storage:** PostgreSQL (optional for storing farmer data)  
- **Hosting:** VPS with GPU support (if needed for AI acceleration)  

---

### **3️⃣ Machine Learning Model (AI for Apple Detection)**
🤖 **AI Tasks:**  
✅ **Detect Apples** – Identify apples at different growth stages (small, green, ripe).  
✅ **Estimate Maturity** – Classify apple color & size for ripeness assessment.  
✅ **Prevent Duplicate Counting** – Use **Optical Flow Tracking** for movement tracking.  
✅ **Calibrate Accuracy** – Farmer can input **reference apple size** for model correction.  

🔧 **Tech Stack:**  
- **Object Detection Model:** YOLOv8 (best for real-time detection)  
- **Color & Maturity Analysis:** HSV color filtering  
- **Tracking & Counting:** Optical Flow (Lucas-Kanade or Farneback)  

---

## **📊 Data Flow Summary**
1️⃣ **App captures a video or image.**  
2️⃣ **Uploads to VPS for processing.**  
3️⃣ **AI detects apples & analyzes yield.**  
4️⃣ **Backend sends results back to the app.**  
5️⃣ **User receives insights & adjusts manual input for accuracy.**  

---

## **📅 Project Milestones**
🔹 **Phase 1 – Prototype (MVP)** 🛠  
✅ Basic app UI (Video upload, API calls).  
✅ Backend API to receive files.  
✅ Simple apple detection model (initial dataset).  

🔹 **Phase 2 – AI Refinement & Accuracy Tuning** 🎯  
✅ Improve apple recognition across different lighting conditions.  
✅ Implement Optical Flow tracking to prevent double counting.  
✅ Add maturity grading based on color analysis.  

🔹 **Phase 3 – Full Deployment & Scaling** 🚀  
✅ Optimize app for offline field usage.  
✅ Deploy backend on a scalable VPS.  
✅ Field testing with farmers for accuracy validation.  

---

## **📝 Open Questions for Refinement**
1. Should we support **offline processing** (limited AI on-device)?  
2. Do farmers need **manual input override** if AI results seem wrong?  
3. Would you like **historical tracking** (compare past yields in-app)?  

---
 

# **API Interface Definition**
## **Overview**
This document defines the API interface for the Pomolobee project, specifying:
- API calls and data exchanged
- Endpoints and request/response format
---
 
   
<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [**API Interface Definition**](#api-interface-definition)
  - [**Overview**](#overview)
  - [Global API Endpoint Overview](#global-api-endpoint-overview)
    - [1     App → Django](#1-app-django)
    - [2     Django → ML](#2-django-ml)
    - [3    ML  →  Django](#3-ml-django)
  - [Document reference](#document-reference)
    - [1   App → Django [App → Django specification]API_1_App_to_Django.md](#1-app-django-app-django-specificationapi1apptodjangomd)
    - [2   Django → ML  [Django → ML specification]API_2_Django_to_ML.md](#2-django-ml-django-ml-specificationapi2djangotomlmd)
    - [3   ML  → Django [ML → Django specification]API_3_ML_to_Django.md](#3-ml-django-ml-django-specificationapi3mltodjangomd)
<!-- TOC END -->
 
</details>

---


## Global API Endpoint Overview



### 1     App → Django

| **Category** | **Endpoints** | **Purpose** | **Caller → Receiver** | **Screen Used In** (**Button Triggering API Call**) |
|-------------|--------------|-------------|----------------------|----------------------------------------|
| **Fetching Orchard Data** | `GET /api/fields/` | Fetch all available fields (orchards). | **App → Django Backend** | **SettingsScreen (🔄 Sync Data Button)** |
| | `GET /api/fruits/` | Fetch all available fruit types. | **App → Django Backend** | **SettingsScreen (🔄 Sync Data Button)** |
| **Location Selection API** | `GET /api/locations/` | Fetch orchard and field details for offline use. | **App → Django Backend** | **SettingsScreen (🔄 Sync Data Button)** |
| **Updating Raw Details** | `PATCH /api/raws/{raw_id}/` | Modify details of a raw (e.g., tree count). | **App → Django Backend** | **SettingsScreen (✏️ Edit Raw Button & 💾 Save Button)** |
| **Updating Field Information** | `PATCH /api/fields/{field_id}/` | Modify details of a field (e.g., name, orientation). | **App → Django Backend** | **SettingsScreen (✏️ Edit Field Button & 💾 Save Button)** |
| **Uploading Images (On Demand)** | `POST /api/images/` | Upload an image for processing (includes `raw_id`). | **App → Django Backend** | **ProcessingScreen (📤 Analyze Button)** |
| **Checking Processing Status** | `GET /api/images/{image_id}/status/` | Check if ML has processed the image. | **App → Django Backend** | **ProcessingScreen (🔄 Refresh Status Button)** |
| **Fetching Estimation Results** | `GET /api/estimations/{image_id}/` | Fetch ML detection results (apple count, confidence, yield). | **App → Django Backend** | **ResultScreen (🔄 Load Estimation Button)** |
| | `GET /api/latest_estimations/` | Fetch latest completed estimations. | **App → Django Backend** | **ResultScreen (🔄 Load Latest Estimations Button)** |
| **Fetching Image List** | `GET /api/images/` | Fetch all uploaded images with their status. | **App → Django Backend** | **ProcessingScreen (🔄 Refresh Status Button)** |
| **Fetching Image Details** | `GET /api/images/{image_id}/details/` | Retrieves metadata of a specific uploaded image. | **App → Django Backend** | **ProcessingScreen, ResultScreen (Clicking on Image Row)** |
| **Deleting an Image** | `DELETE /api/images/{image_id}/` | Delete an uploaded image from the server. | **App → Django Backend** | **ProcessingScreen (🗑️ Delete Image Button)** |
| **Fetching Processing Errors** | `GET /api/images/{image_id}/error_log` | Fetch errors related to image processing. | **App → Django Backend** | **ProcessingScreen (⚠️ View Error Log Button)** | 
| **Fetching History of Estimations** | `GET /api/history/` | Fetch past estimation records. | **App → Django Backend** | **ResultScreen (📜 View History Button)** |
| **Fetching Single Historical Record** | `GET /api/history/{history_id}/` | Retrieve a detailed past estimation result. | **App → Django Backend** | **ResultScreen (📜 View Detailed History Button)** |
| **Request Retry for ML Processing** | `POST /api/retry_processing/` | Request to retry ML processing if it failed. | **App → Django Backend** | **ProcessingScreen (🔄 Retry Processing Button)** |
| | `GET /api/images/{image_id}/ml_result` | App Fetch ML results from Django. | **App → Django** | **Status Polling or Debug Tool** |
| **App fetches ML version** | `GET /api/ml/version/` | Fetch the current ML model version. | **App → Django Backend** | **SettingsScreen  Django fetches the ML version from the ML server and exposes it to the app** |


### 2     Django → ML

| **Category** | **Endpoints** | **Purpose** | **Caller → Receiver** | **Screen Used In** (**Button Triggering API Call**) |
|-------------|--------------|-------------|----------------------|
| **Django → ML API Calls** | `POST /ml/process-image/` | Django sends an image to the ML model for processing. | **Django → ML Model** | **Backend Processing (Automated)** |
| **ML Model Debugging** | `GET /ml/version/` | Fetch the current ML model version. | **Django Backend → ML Model** | **triggered by App GET /api/ml/version/* |


### 3    ML  →  Django


| **Category** | **Endpoints** | **Purpose** | **Caller → Receiver** | **Screen Used In** (**Button Triggering API Call**) |
|-------------|--------------|-------------|----------------------|
| | `POST /api/images/{image_id}/ml_result` | ML returns detection results to Django. | **ML Model → Django** | **After processing completion** |

 
---
 

## Document reference


### 1   App → Django [App → Django specification]API_1_App_to_Django.md
### 2   Django → ML  [Django → ML specification]API_2_Django_to_ML.md
### 3   ML  → Django [ML → Django specification]API_3_ML_to_Django.md
 
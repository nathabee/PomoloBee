
# **📜 API Interface Definition**
## **Overview**
This document defines the API interface for the Pomolobee project, specifying:
- API calls and data exchanged
- Endpoints and request/response format
---
**Table of Content**
<!-- TOC -->
- [**📜 API Interface Definition**](#api-interface-definition)
  - [**Overview**](#overview)
  - [**📌 List of All API Endpoints** ](#list-of-all-api-endpoints)
  - [**📌 API Endpoint Specifications**](#api-endpoint-specifications)
    - [**📌 Fetching Orchard Data API Endpoints**](#fetching-orchard-data-api-endpoints)
    - [**1️⃣ Fetch All Fields (Orchards)**](#1-fetch-all-fields-orchards)
    - [**5️⃣ Fetch All Available Fruit Types**](#5-fetch-all-available-fruit-types)
    - [**1️⃣ Fetch All Fields & Their Raw Data (Location Selection)**](#1-fetch-all-fields--their-raw-data-location-selection)
    - [**2️⃣ Upload an Image for Processing**](#2-upload-an-image-for-processing)
    - [**3️⃣ Check Image Processing Status**](#3-check-image-processing-status)
    - [**4️⃣ Fetch Apple Detection Results**](#4-fetch-apple-detection-results)
    - [**5️⃣ Fetch Latest Completed Estimations**](#5-fetch-latest-completed-estimations)
    - [**6️⃣ Fetch List of Uploaded Images**](#6-fetch-list-of-uploaded-images)
    - [**7️⃣ Fetch Metadata of a Specific Uploaded Image**](#7-fetch-metadata-of-a-specific-uploaded-image)
    - [**8️⃣ Delete an Image**](#8-delete-an-image)
    - [**2️⃣ Fetching a Single Historical Record**](#2-fetching-a-single-historical-record)
    - [**3️⃣ Fetching Processing Errors**](#3-fetching-processing-errors)
    - [**4️⃣ Request Retry for ML Processing**](#4-request-retry-for-ml-processing)
    - [**5️⃣ Updating Raw Details**](#5-updating-raw-details)
    - [**6️⃣ Updating Field Information**](#6-updating-field-information)
    - [**📌 API Specifications for Django ↔ ML Communication, Polling, and Error Handling**](#api-specifications-for-django--ml-communication-polling-and-error-handling)
    - [**1️⃣ Django → ML: Sending Image for Processing**](#1-django--ml-sending-image-for-processing)
    - [**2️⃣ ML → Django: Returning Image Processing Results**](#2-ml--django-returning-image-processing-results)
    - [**3️⃣ ML Model Debugging**](#3-ml-model-debugging)
  - [**4. API Design**  ](#4-api-design)
    - [**API Call Order**  ](#api-call-order)
    - [**Polling Strategy**  ](#polling-strategy)
  - [**Error Handling Strategy**  ](#error-handling-strategy)
<!-- TOC END -->
---

## **📌 List of All API Endpoints** 

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


| **Django → ML API Calls** | `POST /ml/process-image/` | Django sends an image to the ML model for processing. | **Django → ML Model** | **Backend Processing (Automated)** |
| | `GET /api/images/{image_id}/ml_result` | ML returns detection results to Django. | **Django → ML Model** | **Backend Processing (Automated)** |

| **ML Model Debugging** | `GET /api/ml/version/` | Fetch the current ML model version. | **App → Django Backend** | **SettingsScreen (🐛 Debug ML Version Button)** |

---
 

 

## **📌 API Endpoint Specifications**

Here is the **detailed API specification** for fetching orchard data, including **purpose, endpoint, request parameters, and response format** based on your Django models.

---

### **📌 Fetching Orchard Data API Endpoints**

### **1️⃣ Fetch All Fields (Orchards)**
📌 **Purpose:** Retrieve a list of all available agricultural fields.

✅ **Endpoint:**  
```
GET /api/fields/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**
```json
{
    "fields": [
        {
            "id": 1,
            "short_name": "North_Field",
            "name": "North Orchard",
            "description": "Main orchard section for apples.",
            "orientation": "N"
        },
        {
            "id": 2,
            "short_name": "South_Field",
            "name": "South Orchard",
            "description": "Smaller orchard with mixed fruit trees.",
            "orientation": "S"
        }
    ]
}
```

---

### **5️⃣ Fetch All Available Fruit Types**
📌 **Purpose:** Retrieve a list of all available fruit types.

✅ **Endpoint:**  
```
GET /api/fruits/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**
```json
{
    "fruits": [
        {
            "id": 5,
            "short_name": "Golden_Apple",
            "name": "Golden Apple",
            "description": "Sweet yellow apples, ripe in autumn.",
            "yield_start_date": "2024-09-01",
            "yield_end_date": "2024-10-15",
            "yield_avg_kg": 2.5,
            "fruit_avg_kg": 0.3
        },
        {
            "id": 6,
            "short_name": "Red_Apple",
            "name": "Red Apple",
            "description": "Crunchy red apples, available in late summer.",
            "yield_start_date": "2024-07-15",
            "yield_end_date": "2024-08-30",
            "yield_avg_kg": 2.8,
            "fruit_avg_kg": 0.35
        }
    ]
}
```
 
---

 ############################################

### **1️⃣ Fetch All Fields & Their Raw Data (Location Selection)**
📌 **Purpose:** Retrieve **all fields** and their respective **tree rows (Raws)** in a single request.

✅ **Endpoint:**  
```
GET /api/locations/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**
```json
{
    "locations": [
        {
            "field_id": 1,
            "field_name": "North Orchard",
            "orientation": "N",
            "raws": [
                {
                    "raw_id": 101,
                    "short_name": "Row_A",
                    "name": "Row A",
                    "nb_plant": 50,
                    "fruit_id": 5,
                    "fruit_type": "Golden Apple"
                },
                {
                    "raw_id": 102,
                    "short_name": "Row_B",
                    "name": "Row B",
                    "nb_plant": 40,
                    "fruit_id": 6,
                    "fruit_type": "Red Apple"
                }
            ]
        },
        {
            "field_id": 2,
            "field_name": "South Orchard",
            "orientation": "S",
            "raws": [
                {
                    "raw_id": 201,
                    "short_name": "Row_C",
                    "name": "Row C",
                    "nb_plant": 45,
                    "fruit_id": 7,
                    "fruit_type": "Green Apple"
                }
            ]
        }
    ]
}
```

✅ **Response (Error - 404 Not Found)**
```json
{
    "error": "No field and raw data available."
}
```

---

### **2️⃣ Upload an Image for Processing**
📌 **Purpose:** Upload an **image** and associate it with a **specific raw (tree row)** for apple detection.

✅ **Endpoint:**  
```
POST /api/images/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Request Payload:**
| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|---------|-------------|---------------|
| `image` | `file` | ✅ Yes | The image file to be uploaded (JPEG/PNG). |
| `raw_id` | `integer` | ✅ Yes | The **raw (tree row) ID** where the image was taken. |
| `date` | `string (YYYY-MM-DD)` | ✅ Yes | The date when the image was taken. |

✅ **Example Request:**
```bash
curl -X POST "https://server.com/api/images/" \
-H "Content-Type: multipart/form-data" \
-F "image=@apple_photo.jpg" \
-F "raw_id=3" \
-F "date=2024-03-14"
```

✅ **Response (Success - 201 Created)**
```json
{
    "image_id": 24,
    "message": "Image uploaded successfully and queued for processing."
}
```

✅ **Response (Error - Missing Parameters)**
```json
{
    "error": "Image and raw_id are required."
}
```

---

### **3️⃣ Check Image Processing Status**
📌 **Purpose:** Retrieve the **status** of an uploaded image (whether processing is complete or still ongoing).
 The **app periodically checks** if an uploaded image has been processed.

✅ **Endpoint:**  
```
GET /api/images/{image_id}/status/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Path Parameters:**
| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|---------|-------------|---------------|
| `image_id` | `integer` | ✅ Yes | Unique ID of the uploaded image. |

✅ **Response (Success - 200 OK)**
```json
{
    "image_id": 24,
    "status": "done",
    "processed": true,
    "nb_apfel": 15,
    "confidence_score": 0.87
}
```

✅ **Response (Still Processing - 200 OK)**
```json
{
    "image_id": 24,
    "status": "processing",
    "processed": false
}
```

✅ **Response (Error - 404 Not Found)**
```json
{
    "error": "Image not found."
}
```

---

### **4️⃣ Fetch Apple Detection Results**
📌 **Purpose:** Retrieve the **apple count, confidence score, and estimated yield** for a processed image.

✅ **Endpoint:**  
```
GET /api/estimations/{image_id}/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Path Parameters:**
| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|---------|-------------|---------------|
| `image_id` | `integer` | ✅ Yes | Unique ID of the uploaded image. |

✅ **Response (Success - 200 OK)**
```json
{
    "image_id": 24,
    "plant_apfel": 12,
    "plant_kg": 2.4,
    "raw_kg": 48.0,
    "confidence_score": 0.85,
    "status": "done"
}
```

✅ **Response (Error - 404 Not Found)**
```json
{
    "error": "Estimation not found."
}
```

---

### **5️⃣ Fetch Latest Completed Estimations**
📌 **Purpose:** Retrieve the **most recent** completed estimations.

✅ **Endpoint:**  
```
GET /api/latest_estimations/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**
```json
{
    "latest_estimations": [
        {
            "image_id": 24,
            "raw_id": 3,
            "plant_apfel": 12,
            "plant_kg": 2.4,
            "raw_kg": 48.0,
            "confidence_score": 0.85,
            "date": "2024-03-14"
        },
        {
            "image_id": 25,
            "raw_id": 4,
            "plant_apfel": 10,
            "plant_kg": 2.0,
            "raw_kg": 40.0,
            "confidence_score": 0.83,
            "date": "2024-03-13"
        }
    ]
}
```

✅ **Response (Error - 404 Not Found)**
```json
{
    "error": "No recent estimations found."
}
```

---

### **6️⃣ Fetch List of Uploaded Images**
📌 **Purpose:** Retrieve all uploaded images and their statuses.

✅ **Endpoint:**  
```
GET /api/images/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**
```json
{
    "images": [
        { "image_id": 24, "raw_id": 3, "status": "done", "upload_date": "2024-03-10" },
        { "image_id": 25, "raw_id": 5, "status": "processing", "upload_date": "2024-03-12" }
    ]
}
```

---

### **7️⃣ Fetch Metadata of a Specific Uploaded Image**
📌 **Purpose:** Retrieve **detailed metadata** of an uploaded image.

✅ **Endpoint:**  
```
GET /api/images/{image_id}/details/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**
```json
{
    "image_id": 24,
    "raw_id": 3,
    "field_id": 1,
    "fruit_type": "Golden Apple",
    "status": "done",
    "upload_date": "2024-03-10",
    "image_url": "https://server.com/images/24.jpg"
}
```

---

### **8️⃣ Delete an Image**
📌 **Purpose:** Remove an uploaded image from the server.

✅ **Endpoint:**  
```
DELETE /api/images/{image_id}/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**
```json
{
    "message": "Image deleted successfully."
}
```

✅ **Response (Error - 404 Not Found)**
```json
{
    "error": "Image not found."
}
```

---
###########################################

✅ **Endpoint:**  
```
GET /api/history/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**  
```json
{
    "history": [
        {
            "history_id": 12,
            "raw_id": 3,
            "raw_name": "Row A",
            "field_id": 1,
            "field_name": "North Orchard",
            "fruit_type": "Golden Apple",
            "estimated_yield_kg": 50.0,
            "confidence_score": 0.88,
            "date": "2024-03-05"
        },
        {
            "history_id": 13,
            "raw_id": 5,
            "raw_name": "Row B",
            "field_id": 2,
            "field_name": "South Orchard",
            "fruit_type": "Red Apple",
            "estimated_yield_kg": 30.0,
            "confidence_score": 0.75,
            "date": "2024-03-08"
        }
    ]
}
```

✅ **Response (Error - 404 Not Found)**  
```json
{
    "error": "No history records found."
}
```

---

### **2️⃣ Fetching a Single Historical Record**
📌 **Purpose:** Retrieve the **detailed results of a past yield estimation** for a specific record.

✅ **Endpoint:**  
```
GET /api/history/{history_id}/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Path Parameters:**
| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|---------|-------------|---------------|
| `history_id` | `integer` | ✅ Yes | Unique ID of the estimation history record. |

✅ **Response (Success - 200 OK)**
```json
{
    "history_id": 12,
    "raw_id": 3,
    "raw_name": "Row A",
    "field_id": 1,
    "field_name": "North Orchard",
    "fruit_type": "Golden Apple",
    "estimated_yield_kg": 50.0,
    "confidence_score": 0.88,
    "image_url": "https://server.com/images/processed_12.jpg",
    "timestamp": "2024-03-05T12:00:00"
}
```

✅ **Response (Error - 404 Not Found)**  
```json
{
    "error": "Estimation history not found."
}
```

---

### **3️⃣ Fetching Processing Errors**
📌 **Purpose:** Retrieve **error logs** related to a specific image processing failure.

✅ **Endpoint:**  
```
GET /api/images/{image_id}/error_log/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Path Parameters:**
| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|---------|-------------|---------------|
| `image_id` | `integer` | ✅ Yes | Unique ID of the uploaded image. |

✅ **Response (Success - 200 OK)**
```json
{
    "image_id": 24,
    "status": "failed",
    "error_log": "ML model failed to detect apples due to poor image quality.",
    "timestamp": "2024-03-14T08:45:00"
}
```

✅ **Response (Error - 404 Not Found)**  
```json
{
    "error": "No error log found for this image."
}
```

---

### **4️⃣ Request Retry for ML Processing**
📌 **Purpose:** **Retry ML processing** if an image failed to process due to an issue.

✅ **Endpoint:**  
```
POST /api/retry_processing/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Request Payload:**
| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|---------|-------------|---------------|
| `image_id` | `integer` | ✅ Yes | Unique ID of the uploaded image. |

✅ **Example Request:**
```bash
curl -X POST "https://server.com/api/retry_processing/" \
-H "Content-Type: application/json" \
-d '{"image_id": 24}'
```

✅ **Response (Success - 200 OK)**
```json
{
    "message": "Image processing retry has been requested."
}
```

✅ **Response (Error - 404 Not Found)**  
```json
{
    "error": "Image not found or already processed successfully."
}
```

---

### **5️⃣ Updating Raw Details**
📌 **Purpose:** Update the **number of trees** in a given raw or modify other attributes.

✅ **Endpoint:**  
```
PATCH /api/raws/{raw_id}/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Path Parameters:**
| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|---------|-------------|---------------|
| `raw_id` | `integer` | ✅ Yes | Unique ID of the raw (tree row) to be updated. |

✅ **Request Payload (Optional Fields):**
```json
{
    "name": "Updated Row A",
    "nb_plant": 55
}
```

✅ **Response (Success - 200 OK)**
```json
{
    "raw_id": 3,
    "message": "Raw details updated successfully."
}
```

✅ **Response (Error - 404 Not Found)**  
```json
{
    "error": "Raw not found."
}
```

---

### **6️⃣ Updating Field Information**
📌 **Purpose:** Modify the **name, orientation, or other attributes** of a specific field.

✅ **Endpoint:**  
```
PATCH /api/fields/{field_id}/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Path Parameters:**
| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|---------|-------------|---------------|
| `field_id` | `integer` | ✅ Yes | Unique ID of the field to be updated. |

✅ **Request Payload (Optional Fields):**
```json
{
    "name": "Updated North Orchard",
    "orientation": "NE"
}
```

✅ **Response (Success - 200 OK)**
```json
{
    "field_id": 1,
    "message": "Field details updated successfully."
}
```

✅ **Response (Error - 404 Not Found)**  
```json
{
    "error": "Field not found."
}
```

---
############################################
### **📌 API Specifications for Django ↔ ML Communication, Polling, and Error Handling**

---

### **1️⃣ Django → ML: Sending Image for Processing**
📌 **Purpose:** Django sends an uploaded image to the ML model for apple detection.

✅ **Endpoint:**  
```
POST /process-image/
```
✅ **Caller → Receiver:**  
- **Django Backend → ML Model**

✅ **Request Payload:**
| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|---------|-------------|---------------|
| `image_url` | `string` | ✅ Yes | URL/path of the uploaded image stored by Django. |
| `image_id` | `integer` | ✅ Yes | Unique ID of the image in the Django database. |

✅ **Example Request:**
```json
{
    "image_url": "https://server.com/images/image_24.jpg",
    "image_id": 24
}
```

✅ **Response (Success - 200 OK)**
```json
{
    "message": "Image received, processing started."
}
```

✅ **Response (Error - 400 Bad Request)**
```json
{
    "error": "Invalid image URL or image ID."
}
```

---


### **2️⃣ ML → Django: Returning Image Processing Results**
📌 **Purpose:** The ML model returns **apple detection results** to Django.

✅ **Endpoint:**  
```
GET /api/images/{image_id}/ml_result
```
✅ **Caller → Receiver:**  
- **Django Backend → ML Model**

✅ **Path Parameters:**
| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|---------|-------------|---------------|
| `image_id` | `integer` | ✅ Yes | Unique ID of the image whose result is requested. |

✅ **Response (Success - 200 OK)**
```json
{
    "image_id": 24,
    "nb_apples": 12,
    "confidence_score": 0.89,
    "processed": true
}
```

✅ **Response (Error - 404 Not Found)**
```json
{
    "error": "ML results not found for this image."
}
```

---

### **3️⃣ ML Model Debugging**
📌 **Purpose:** Fetch the **current ML model version** and status.

✅ **Endpoint:**  
```
GET /api/ml/version/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**
```json
{
    "model_version": "v1.2.5",
    "status": "active",
    "last_updated": "2024-03-10T14:00:00"
}
```

✅ **Response (Error - 500 Internal Server Error)**
```json
{
    "error": "ML service unavailable."
}
```

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


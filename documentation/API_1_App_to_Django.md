
# **App -> Django API Interface Definition**
## **Overview**
This document defines the API interface for the Pomolobee project, specifying:
- API calls and data exchanged
- Endpoints and request/response format
---

<details>
<summary>Table of Content</summary>
 
<!-- TOC -->
- [**App -> Django API Interface Definition**](#app-django-api-interface-definition)
  - [**Overview**](#overview)
  - [Section A Orchard Tree Data Fields Fruits Locations](#section-a-orchard-tree-data-fields-fruits-locations)
    - [**Fetch All Fields Orchards**](#fetch-all-fields-orchards)
    - [**Fetch All Available Fruit Types**](#fetch-all-available-fruit-types)
    - [**Fetch All Fields Their Raw Data Location Selection**](#fetch-all-fields-their-raw-data-location-selection)
  - [Section B Image Upload ML Processing](#section-b-image-upload-ml-processing)
    - [**Upload an Image for Processing**](#upload-an-image-for-processing)
    - [**Request Retry for ML Processing**](#request-retry-for-ml-processing)
    - [**Fetch the Current ML Model Version**](#fetch-the-current-ml-model-version)
    - [**Check Image Processing Status**](#check-image-processing-status)
  - [Section C Estimations Yield Results](#section-c-estimations-yield-results)
    - [**Fetch fruit Detection Results**](#fetch-fruit-detection-results)
    - [**Fetch Latest Completed Estimations**](#fetch-latest-completed-estimations)
    - [**Fetch Metadata of a Specific Uploaded Image**](#fetch-metadata-of-a-specific-uploaded-image)
    - [**Delete an Image**](#delete-an-image)
  - [Section D History Analytics](#section-d-history-analytics)
    - [**Fetching all Estimation Records**](#fetching-all-estimation-records)
    - [**Fetching a Estimation Record for an image**](#fetching-a-estimation-record-for-an-image)
  - [**4. API Design**](#4-api-design)
    - [Query Examples](#query-examples)
    - [**API Call Order**](#api-call-order)
    - [**Polling Strategy**](#polling-strategy)
  - [**Error Handling Strategy in DJANGO**](#error-handling-strategy-in-django)
  - [**JSON Format Conventions**](#json-format-conventions)
<!-- TOC END -->
 
</details>

---
 

## Section A Orchard Tree Data Fields Fruits Locations
 
📌 **Purpose:** Sync orchard and tree data used for mapping & selection in the app.
 

### **Fetch All Fields Orchards**
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
  "status": "success",
  "data": {

    "fields": [
        {
            "field_id": 1,
            "short_name": "North_Field",
            "name": "North Orchard",
            "description": "Main orchard section for fruit.",
            "orientation": "N"
        },
        {
            "field_id": 2,
            "short_name": "South_Field",
            "name": "South Orchard",
            "description": "Smaller orchard with mixed fruit trees.",
            "orientation": "S"
        }
     ]
    }
}
```

---

### **Fetch All Available Fruit Types**
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

  "status": "success",
  "data": {
    "fruits": [
        {
            "fruit_id": 5,
            "short_name": "Golden_fruit",
            "name": "Golden fruit",
            "description": "Sweet yellow fruit, ripe in autumn.",
            "yield_start_date": "2024-09-01",
            "yield_end_date": "2024-10-15",
            "yield_avg_kg": 2.5,
            "fruit_avg_kg": 0.3
        },
        {
            "fruit_id": 6,
            "short_name": "Red_fruit",
            "name": "Red fruit",
            "description": "Crunchy red fruit, available in late summer.",
            "yield_start_date": "2024-07-15",
            "yield_end_date": "2024-08-30",
            "yield_avg_kg": 2.8,
            "fruit_avg_kg": 0.35
        }
    ]
}
}
```
 
--- 

### **Fetch All Fields Their Raw Data Location Selection**
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

  "status": "success",
  "data": {
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
                    "fruit_type": "Golden fruit"
                },
                {
                    "raw_id": 102,
                    "short_name": "Row_B",
                    "name": "Row B",
                    "nb_plant": 40,
                    "fruit_id": 6,
                    "fruit_type": "Red fruit"
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
                    "fruit_type": "Green fruit"
                }
            ]
        }
    ]
  }
}
```

✅ **Response (Error - 404 Not Found)**
```json
 {
  "error": {
    "code": "404_NOT_FOUND",
    "message": "No field and raw data available."
  }
}
```

---


## Section B Image Upload ML Processing
📌 **Purpose:** Upload image, send to ML, track progress, and fetch detection results.


### **Upload an Image for Processing**
📌 **Purpose:** Upload an **image** and associate it with a **specific raw (tree row)** for fruit detection.

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
-F "image=@fruit_photo.jpg" \
-F "raw_id=3" \
-F "date=2024-03-14"
```

✅ **Response (Success - 201 Created)**
```json
{

  "status": "success",
  "data": {
    "image_id": 24,
    "message": "Image uploaded successfully and queued for processing."
}
}
```

✅ **Response (Error - Missing Parameters)**
```json
{
  "error": {
    "code": "MISSING_PARAMETER",
    "message": "Image and raw_id are required."
  }
}

{
  "error": {
    "code": "ML_UNAVAILABLE",
    "message": "ML service unavailable. Image has been saved and can be retried.",
    "image_id": 24
  }
}

```

---

### **Request Retry for ML Processing**
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

  "status": "success",
  "data": {
    "message": "Image processing retry has been requested."
}
}
```

✅ **Response (Error - 404 Not Found)**  
```json
{
  "error": {
    "code": "404_NOT_FOUND",
    "message": "Image not found."
  }
}
or 
{
  "error": {
    "code": "ALREADY_PROCESSED",
    "message": "This image has already been processed and cannot be retried."
  }
}

```

---

---

### **Fetch the Current ML Model Version**
📌 **Purpose:** Django fetches the ML version from the ML server and exposes it to the app.

✅ **Endpoint:**  
```
GET /api/ml/version/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**
```json
{
  "status": "success",
  "data": 
        {
            "model_version": "v1.2.5",
            "status": "active",
            "last_updated": "2024-03-10T14:00:00"
        }
  }
```

✅ **Response (Error - 500 Internal Server Error)**
```json
{
  "error": {
    "code": "ML_UNAVAILABLE",
    "message": "ML service unavailable"
  }
}
```

---

   




### **Check Image Processing Status**
📌 **Purpose:** Retrieve the **status** of an uploaded image (whether processing is complete or still ongoing).
 The **app periodically checks** if an uploaded image has been processed.

✅ **Endpoint:**  
```
GET /api/images/{image_id}/details 
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

  "status": "success",
  "data": {
    "image_id": 24,
    "status": "done",
    "processed": true,
    "nb_fruit": 15,
    "confidence_score": 0.87
}
}
```

✅ **Response (Still Processing - 200 OK)**
```json
{

  "status": "success",
  "data": {
    "image_id": 24,
    "status": "processing",
    "processed": false
}
}
```

✅ **Response (Error - 404 Not Found)**
```json
{
  "error": {
    "code": "404_NOT_FOUND",
    "message": "Image not found."
  }
}
```

---

## Section C Estimations Yield Results
📌 **Purpose:** Fetch estimation/yield results computed by ML and stored in Django.


### **Fetch fruit Detection Results**
📌 **Purpose:** Retrieve the **fruit count, confidence score, and estimated yield** for a processed image.

✅ **Endpoint:**  
```
GET /api/images/{image_id}/estimations/
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

  "status": "success",
  "data": {
  "image_id": 24,
  "plant_fruit": 12,
  "plant_kg": 2.4,
  "raw_kg": 48.0,
  "confidence_score": 0.85,
  "maturation_grade": 0.4,
  "source": "Machine Learning from image",
  "field_id": 1,
  "field_name": "ChampMaison",
  "raw_id": 3,
  "raw_name": "C1-R3",
  "fruit_type": "Swing on CG1",
  "status": "done"
}
}
```

✅ **Response (Error - 404 Not Found)**
```json
{
  "error": {
    "code": "404_NOT_FOUND",
    "message": "Estimation not found."
  }
}
```

---



### **Fetch Latest Completed Estimations**
📌 **Purpose:** Retrieve the estimations associated to a field.

✅ **Endpoint:**  
```
GET /api/fields/{field_id}/estimations/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**
```json
{

  "status": "success",
    "data": {
        "latest_estimations": [
            {
                "image_id": 24,
                "plant_fruit": 12,
                "plant_kg": 2.4,
                "raw_kg": 48.0,
                "confidence_score": 0.85,
                "date": "2024-03-01"
                "field_id": 1,
                "field_name": "ChampMaison",
                "raw_id": 3,
                "raw_name": "C1-R3",
                "fruit_type": "Swing on CG1",
                "status": "done" 
            },
            {
                "image_id": 25,
                "raw_id": 4,
                "plant_fruit": 10,
                "plant_kg": 2.0,
                "raw_kg": 40.0,
                "confidence_score": 0.83,
                "date": "2024-03-13"
                "field_id": 1,
                "field_name": "ChampMaison",
                "raw_id": 3,
                "raw_name": "C1-R3",
                "fruit_type": "Swing on CG1",
                "status": "done"
            }
        ]
    }
}
```

✅ **Response (Error - 404 Not Found)**
```json
{
  "error": {
    "code": "404_NOT_FOUND",
    "message": "No estimation found."
  }
}
```
 
---

### **Fetch Metadata of a Specific Uploaded Image**
📌 **Purpose:** Retrieve **detailed metadata** of an uploaded image inclusive status.

✅ **Endpoint:**  
```
GET /api/images/{image_id}/details/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**
```json
{

  "status": "success",
    "data": {
        "image_id": 24,
        "raw_id": 3,
        "field_id": 1,
        "fruit_type": "Golden fruit",
        "status": "done",
        "upload_date": "2024-03-10",
        "image_url": "https://server.com/images/24.jpg"
    }
}
```

---

### **Delete an Image**
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

  "status": "success",
    "data": {
        "message": "Image deleted successfully."
    }
}
```

✅ **Response (Error - 404 Not Found)**
```json
{
  "error": {
    "code": "404_NOT_FOUND",
    "message": "Image not found."
  }
}
```
---

 
--- 
 

## Section D History Analytics
📌 **Purpose:**  Retrieve previously estimated yields and details.


### **Fetching all Estimation Records**
📌 **Purpose:** Retrieve the **detailed results of all past yield estimation for a field**  
✅ **Endpoint:**  
```
GET /api/fields/{field_id}/estimations/
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Response (Success - 200 OK)**  
```json
{
  "status": "success",
  "data": {
    "estimations": [
        {
            "estimation_id": 12,
            "image_id": 12,
            "raw_id": 3,
            "raw_name": "Row A",
            "field_id": 1,
            "field_name": "North Orchard",
            "fruit_type": "Golden fruit",
            "estimated_yield_kg": 50.0,
            "confidence_score": 0.88,
            "date": "2024-03-05"
        },
        {
            "estimation_id": 13,
            "image_id": 13,
            "raw_id": 5,
            "raw_name": "Row B",
            "field_id": 2,
            "field_name": "South Orchard",
            "fruit_type": "Red fruit",
            "estimated_yield_kg": 30.0,
            "confidence_score": 0.75,
            "date": "2024-03-08"
        }
    ]
  }
}
```

✅ **Response (Error - 404 Not Found)**  
```json
 {
  "error": {
    "code": "404_NOT_FOUND",
    "message": "No estimation records found."
  }
}
```


 
---

### **Fetching a Estimation Record for an image**
📌 **Purpose:** Retrieve the **detailed results of a past yield estimation** for a specific record.

✅ **Endpoint:**  
```
GET /api/images/{image_id}/estimations
```
✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Path Parameters:**
| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|---------|-------------|---------------|
| `image_id` | `integer` | ✅ Yes | ID of the image used for estimation record. |

✅ **Response (Success - 200 OK)**
```json

{
  "status": "success",
  "data": {
        "image_id": 12,
        "raw_id": 3,
        "raw_name": "Row A",
        "field_id": 1,
        "field_name": "North Orchard",
        "fruit_type": "Golden fruit",
        "estimated_yield_kg": 50.0,
        "confidence_score": 0.88,
        "image_url": "https://server.com/images/processed_12.jpg",
        "timestamp": "2024-03-05T12:00:00"
    }
}
```

✅ **Response (Error - 404 Not Found)**  
```json

 {
  "error": {
    "code": "404_NOT_FOUND",
    "message": "Estimation not found for this image."
  }
}
```

---

 
## **4. API Design**


###  Query Examples
```sh
curl -X GET "https://server.com/api/images/24/details/"
```

```bash
curl -X POST "https://server.com/api/images/" \
-H "Content-Type: multipart/form-data" \
-F "image=@fruit_photo.jpg" \
-F "raw_id=3" \
-F "date=2024-03-14"
```

### **API Call Order**
📌 `POST /api/images/` (Upload Image)  
📌 `GET /api/images/{image_id}/details` (Check Processing Status)  
📌 `GET /api/images/{image_id}/estimations` (Fetch Estimation Results)  

---

### **Polling Strategy**
📌 The app checks `GET /api/images/{image_id}/details` every **minute**.  
📌 If `status = "done"`, the app fetches results.  
📌 If the process takes longer than **5 retries (5 minutes)**, the app should **show a warning**.  
📌 If ML takes longer than 5 minutes, Django should **log the delay** and optionally **send a retry request to ML**.  


🔹 **Why?**  
- Prevents infinite polling loops.  
- Ensures the user is **not left waiting indefinitely**.  

---

## **Error Handling Strategy in DJANGO**
📌 **What if ML processing fails?**  
- If ML **returns an error**, Django should mark `processed = false` in `Image`.  
- The app should **stop polling after 5 attempts** and **display an error message**.

📌 **What if the app sends an invalid image?**  
- Django should return `400 Bad Request` if the image format is incorrect.  
- The app should prompt the user to upload a valid image.


## **JSON Format Conventions**

📌 **IMPORTANT : see documentation  API** [API specification](API.md) defining :
- list of existing error code
- format and naming convention  
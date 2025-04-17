
# **App -> Django API Interface Definition**
## **Overview**
This document defines the API interface for the Pomolobee project, specifying:
- API calls and data exchanged
- Endpoints and request/response format

🔗 All media asset paths (e.g., image_url, svg_map_url) are returned as relative paths. 
The app or ML service must prepend its configured Django base URL.

Example of Django json response (snapshots made during Django integration or non regression Test) :
 
-  **API Example** [JSON example](/PomoloBeeDjango/tests/snapshots)


- The background image (e.g. JPEG) must be created/exported with the exact same coordinate system as the SVG viewBox. If the SVG has viewBox="0 0 800 1000", then the background image must be 800px wide and 1000px tall.

---

<details>
<summary>Table of Content</summary>
 
<!-- TOC -->
- [**App -> Django API Interface Definition**](#app-django-api-interface-definition)
  - [**Overview**](#overview)
  - [Section A Fetch Orchard configuration Fruits Locations](#section-a-fetch-orchard-configuration-fruits-locations)
    - [Fetch All Available Fruit Types](#fetch-all-available-fruit-types)
    - [Fetch All Fields Their Row Data Location Selection](#fetch-all-fields-their-row-data-location-selection)
    - [Fetch the Current ML Model Version](#fetch-the-current-ml-model-version)
  - [Section B Image Upload ML Processing](#section-b-image-upload-ml-processing)
    - [**Upload an Image for Processing**](#upload-an-image-for-processing)
    - [**Request Retry for ML Processing**](#request-retry-for-ml-processing)
    - [**Check Image Processing Details and Status**](#check-image-processing-details-and-status)
    - [**Delete an Image**](#delete-an-image)
  - [Section C Estimations Yield Results](#section-c-estimations-yield-results)
    - [MANUAL Estimation](#manual-estimation)
    - [Fetch fruit Detection Results](#fetch-fruit-detection-results)
  - [Section D History Analytics](#section-d-history-analytics)
    - [List All Uploaded Images](#list-all-uploaded-images)
    - [Fetching all Estimation Records in a field](#fetching-all-estimation-records-in-a-field)
  - [Section E Media Asset Usage](#section-e-media-asset-usage)
    - [**Fetch Media Assets for Each Field**](#fetch-media-assets-for-each-field)
  - [**4. API Design**](#4-api-design)
    - [Query Examples](#query-examples)
    - [URL acess](#url-acess)
    - [**API Call Order**](#api-call-order)
    - [**Polling Strategy**](#polling-strategy)
  - [**Error Handling Strategy in DJANGO**](#error-handling-strategy-in-django)
  - [**JSON Format Conventions**](#json-format-conventions)
<!-- TOC END -->
 
</details>

---
 

## Section A Fetch Orchard configuration Fruits Locations
 
📌 **Purpose:** Sync orchard and tree data used for mapping & selection in the app.
 
### Fetch All Available Fruit Types
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

### Fetch All Fields Their Row Data Location Selection
📌 **Purpose:** Retrieve **all fields** and their respective **tree rows (Rows)** in a single request.

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
        "field": {
          "field_id": 1,
          "short_name": "C1",
          "name": "ChampMaison",
          "description": "Champ situé sur la parcelle de la maison",
          "orientation": "NW",
          "svg_map_url": "/media/fields/svg/C1_map.svg",
          "background_image_url": "/media/fields/background/C1.jpeg"
        },
        "rows": [
          {
            "row_id": 1,
            "short_name": "C1-R1",
            "name": "Rang 1 cote maison Swing 1",
            "nb_plant": 38,
            "fruit_id": 1,
            "fruit_type": "Cultivar Swing on CG1"
          },
          {
            "row_id": 2,
            "short_name": "C1-R2",
            "name": "Rang 2 cote maison Swing 2",
            "nb_plant": 40,
            "fruit_id": 1,
            "fruit_type": "Cultivar Swing on CG1"
          }
        ]
      },
      {
        "field": {
          "field_id": 2,
          "short_name": "C2",
          "name": "ChampSud",
          "description": "Champ situé au sud de la propriété, très ensoleillé.",
          "orientation": "S",
          "svg_map_url": "/media/fields/svg/default_map.svg",
          "background_image_url": null
        },
        "rows": []
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
    "message": "No field and row data available."
  }
}
```

---

### Fetch the Current ML Model Version
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
  "data": {
      "last_updated": "2024-04-16T14:00:00",
      "model_version": "v1.4.16",
      "status": "active"
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

## Section B Image Upload ML Processing
📌 **Purpose:** Upload image, send to ML, track progress, and fetch detection results.


### **Upload an Image for Processing**
📌 **Purpose:** Upload an **image** and associate it with a **specific row (tree row)** for fruit detection.

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
| `row_id` | `integer` | ✅ Yes | The **row (tree row) ID** where the image was taken. |
| `date` | `string (YYYY-MM-DD)` | ✅ Yes | The date when the image was taken. |

✅ **Example Request:**
```bash
curl -X POST "https://server.com/api/images/" \
-H "Content-Type: multipart/form-data" \
-F "image=@fruit_photo.jpg" \
-F "row_id=3" \
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
    "message": "Image and row_id are required."
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


### **Check Image Processing Details and Status**
📌 **Purpose:** Retrieve the **status** of an uploaded image (whether processing is complete or still ongoing) and other details.
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

✅ **Response (Success )**
```json
{

  "status": "success",
  "data": {
    "image_id": 3,
    "row_id": 1,
    "field_id": 1,
    "xy_location": null,
    "fruit_type": "Cultivar Swing on CG1",
    "user_fruit_plant": null,
    "upload_date": "2025-04-17",
    "date": "2024-03-14",
    "image_url": "/media/images/image-3.jpg",
    "original_filename": "orchard.jpg",
    "processed": true,
    "processed_at": "2025-04-17T08:12:45",
    "status": "Done"
}
}
```
 


✅ **Response (Still Processing )**
```json
{

  "status": "success",
  "data": {
    "image_id": 24,
    "row_id": 3,
    "date":  "2024-03-05",    
    "field_id": 1,
    "fruit_type": "Golden fruit",
    "upload_date": "2024-03-10",
    "image_url": "/media/images/images-24.jpg",
    "original_filename": "orchard20251201.jpg",
    "status": "Processing",
    "processed": false
}
}
```

✅ **Response (Error During process )**
```json
{

  "status": "success",
  "data": {
    "image_id": 24,
    "row_id": 3,
    "date":  "2024-03-05",    
    "field_id": 1,
    "fruit_type": "Golden fruit",
    "upload_date": "2024-03-10",
    "image_url": "/media/images/images-24.jpg",
    "original_filename": "orchard20251201.jpg",
    "status": "Failed",
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
✅ **Response (Success - 200 OK)**
```json
{
  "status": "success",
  "data": {
    "message": "Image deleted successfully.",
    "warning": "Could not delete file: [Errno 2] No such file or directory"
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
📌 **Purpose:** Fetch estimation/yield results computed by Django based on ML or manual fruit counting 


---


### MANUAL Estimation
```
POST /api/manual_estimation/
```

📌 **Purpose:**  
Post a yield estimation manually (without ML) with optional image to locate estimation.

---

#### **Request Format**

📦 **Content-Type:** `multipart/form-data`

📩 **Form Fields:**

| Field              | Type        | Required | Description |
|-------------------|-------------|----------|-------------|
| `row_id`          | integer     | ✅ Yes   | ID of the row the estimation is for |
| `date`            | string      | ✅ Yes   | Format: `YYYY-MM-DD`. Date of estimation |
| `xy_location`     | string      | ❌ No    | Optional location within row (e.g., `"52.4,17.8"`) |
| `fruit_plant`        | float       | ✅ Yes   | Number of fruits counted manually per plant|
| `confidence_score`| float       | ❌ No    | User confidence in their estimation |
| `maturation_grade`| float       | ❌ No    | Optional maturity index (e.g., 0.0 to 1.0) |
| `image`           | file (JPEG/PNG) | ❌ No | Optional image file. If omitted, a default will be used (`image_default.jpg`) |

---

#### **Example with image**

```bash
curl -X POST http://localhost:8000/api/manual_estimation/ \
  -F "row_id=15" \
  -F "date=2025-04-15" \
  -F "xy_location=52.4,17.8" \
  -F "fruit_plant=11" \
  -F "confidence_score=0.6" \
  -F "maturation_grade=0.3" \
  -F "image=@orchard.jpg"
```

---

#### **Example no image**

```bash
curl -X POST http://localhost:8000/api/manual_estimation/ \
  -F "row_id=15" \
  -F "date=2025-04-15" \
  -F "fruit_plant=11" \
  -F "confidence_score=0.6"
```

---
 
#### **Response Success 201 Created**

```json
{
  "status": "success",
  "data": {
    "image_id": 14,
    "estimation_id": 11,
    "estimations": {
      "estimation_id": 11,
      "image_id": 14,
      "date": "2024-04-15",
      "timestamp": "2025-04-16T13:00:02",
      "row_id": 1,
      "row_name": "Rang 1 cote maison Swing 1",
      "field_id": 1,
      "field_name": "ChampMaison",
      "fruit_type": "Cultivar Swing on CG1",
      "plant_kg": 1.8,
      "row_kg": 68.4,
      "maturation_grade": 0,
      "confidence_score": 0.6,
      "source": "User manual estimation",
      "fruit_plant": 9,
      "status": "manual"
    }
  }
}

```

---

#### **Response Missing Fields 400 Bad Request**

```json
{
  "error": {
    "code": "MISSING_FIELDS",
    "message": "Required fields: row_id, date, fruit_plant"
  }
}
```

--- 

---
### Fetch fruit Detection Results
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
    "field_id": 1,
    "field_name": "ChampMaison",
    "row_id": 3,
    "row_name": "C1-R3",
    "fruit_type": "Swing on CG1",
    "date": "2024-03-01",
    "fruit_plant": 120,
    "plant_kg": 24,
    "row_kg": 4800,
    "confidence_score": 0.85,
    "maturation_grade": 0.4,
    "source": "Machine Learning (Image)",
    "status": "Done"
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

 

## Section D History Analytics
📌 **Purpose:**  Retrieve previously estimated yields and details.  Synchronize image and estimation

 

### List All Uploaded Images
📌 **Purpose:** Retrieve a **paginated list of all uploaded images**, optionally filtered by field, row, or date.

✅ **Endpoint:**  
```
GET /api/images/list/
```

✅ **Caller → Receiver:**  
- **App → Django Backend**

✅ **Query Parameters   for `/api/images/list/`**

| **Parameter** | **Type** | **Description** |
|--------------|----------|-----------------|
| `field_id`   | `integer` | ✅ Optional. Get all images for this field (ignores row_id if not set). |
| `row_id`     | `integer` | ✅ Optional. Get all images for this specific row (more specific than field). |
| `date`       | `YYYY-MM-DD` | ✅ Optional. Filter by capture date (can be used with `row_id` or `field_id`). |
| `limit`      | `integer` | ✅ Optional. Default: 100 |
| `offset`     | `integer` | ✅ Optional. Default: 0 |

🧠 **Note:** `row_id` is more specific than `field_id`, so if both are provided, the filter narrows to just the row.

---

#### Example Query URLs

- All images (default):
  ```
  GET /api/images/list/
  ```

- All images for field `C1`:
  ```
  GET /api/images/list/?field_id=1
  ```

- All images for row `R3` in field C1:
  ```
  GET /api/images/list/?row_id=3
  ```

- Images for row `C1-R3` on March 14:
  ```
  GET /api/images/list/?row_id=3&date=2024-03-14
  ```

- Paginated results:
  ```
  GET /api/images/list/?limit=50&offset=100
  ```

---


✅ **Response (Success - 200 OK)**
```json
{
  "status": "success",
  "data": {
    "total": 9,
    "limit": 100,
    "offset": 0,
    "images": [

      {
        "image_id": 1,
        "row_id": 1,
        "field_id": 1,
        "xy_location": "",
        "fruit_type": "Cultivar Swing on CG1",
        "user_fruit_plant": 9,
        "upload_date": "2025-04-17",
        "date": "2024-04-15",
        "image_url": "/media/images/image_default.jpg",
        "original_filename": null,
        "processed": true,
        "processed_at": "2025-04-17T08:12:42",
        "status": "Done"
      }, 
      {
        "image_id": 4,
        "row_id": 15,
        "field_id": 1,
        "xy_location": null,
        "fruit_type": "Cultivar Pitch on M9",
        "user_fruit_plant": 105,
        "upload_date": "2025-04-17",
        "date": "2024-03-14",
        "image_url": "/media/images/image-4.jpg",
        "original_filename": "orchard.jpg",
        "processed": true,
        "processed_at": "2025-04-17T08:12:50",
        "status": "Done"
      },
      {
        "image_id": 5,
        "row_id": 25,
        "field_id": 1,
        "xy_location": null,
        "fruit_type": "Cultivar Early Crunch on M9 Nakab",
        "user_fruit_plant": 105,
        "upload_date": "2025-04-17",
        "date": "2024-03-14",
        "image_url": "/media/images/image-5.jpg",
        "original_filename": "orchard.jpg",
        "processed": false,
        "processed_at": null,
        "status": "Processing"
      }
    ]
  }
}

```

✅ **Response (Error - Invalid Query Param)**
```json
{
  "error": {
    "code": "INVALID_INPUT",
    "message": "Invalid date format. Expected YYYY-MM-DD."
  }
}
```
---



### Fetching all Estimation Records in a field
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
                  "estimation_id": 3,
                  "image_id": 3,
                  "date": "2024-03-14",
                  "timestamp": "2025-04-17T08:12:45",
                  "row_id": 1,
                  "row_name": "Rang 1 cote maison Swing 1",
                  "field_id": 1,
                  "field_name": "ChampMaison",
                  "fruit_type": "Cultivar Swing on CG1",
                  "plant_kg": 20,
                  "row_kg": 760,
                  "maturation_grade": 0,
                  "confidence_score": 0.85,
                  "source": "Machine Learning (Image)",
                  "fruit_plant": 100,
                  "status": "done"
                },
                {
                  "estimation_id": 2,
                  "image_id": 2,
                  "date": "2024-04-15",
                  "timestamp": "2025-04-17T08:12:42",
                  "row_id": 1,
                  "row_name": "Rang 1 cote maison Swing 1",
                  "field_id": 1,
                  "field_name": "ChampMaison",
                  "fruit_type": "Cultivar Swing on CG1",
                  "plant_kg": 1.6,
                  "row_kg": 60.8,
                  "maturation_grade": 0,
                  "confidence_score": 0.7,
                  "source": "User manual estimation",
                  "fruit_plant": 8,
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
    "message": "No estimation records found."
  }
}
```


 

---
 

## Section E Media Asset Usage

📌 **Purpose:**  
Explain how the **app retrieves SVG maps and background images** for orchard fields from the `media/` folder.

---

### **Fetch Media Assets for Each Field**

✅ **Media is NOT embedded** in any endpoint. The API provides **URLs to static assets**, and the **frontend must download them manually** when needed.

✅ **These URLs are returned in**: 
- `GET /api/locations/`
- `GET /api/images/{image_id}/details/`

✅ Example snippet from the `/api/fields/` response:
```json
{
  "field_id": 1,
  "short_name": "North_Field",
  "name": "North Orchard",
  "svg_map_url": "/media/fields/svg/North_Field_map.svg",
  "background_image_url": "/media/fields/background//North_Field_background.jpg"
}
```

---
 

-
 

## **4. API Design**


###  Query Examples
```sh
curl -X GET "https://server.com/api/images/24/details/"
```

```bash
curl -X POST "https://server.com/api/images/" \
-H "Content-Type: multipart/form-data" \
-F "image=@fruit_photo.jpg" \
-F "row_id=3" \
-F "date=2024-03-14"
```

### URL acess

Split `DJANGO_API_URL` and `DJANGO_MEDIA_URL`

#### Typical Production Setup
| Purpose         | Example URL                        |
|----------------|-------------------------------------|
| Django API      | `https://api.pomolobee.com/`     |
| Media (images)  | `https://media.pomolobee.com/` |



 | Purpose         | Example URL                        |
|----------------|-------------------------------------|
| Django API      | `http://127.0.0.1:8000`     |
| Media (images)  | `http://127.0.0.1:8000` |

 

| Asset Type       | Field Key                | Fetch Timing        | Notes |
|------------------|--------------------------|---------------------|-------|
| **SVG Map**      | `svg_map_url`            | On field view open  | Required for interaction |
| **Background**   | `background_image_url`   | Optional: toggle or lazy load | Can skip if bandwidth is a concern |
| **image**         | `image_url`             | Optional: toggle  | Can skip if bandwidth is a concern |

---
 
#### Media access
- Actual value from API:
/media/fields/svg/C1_map.svg

- App must convert to:
{DJANGO_MEDIA_URL}/media/fields/svg/C1_map.svg
 
#### Service access
- ActuAcess to API:
GET /api/images/{image_id}/details/

- App must convert to:
GET {DJANGO_API_URL}/api/images/{image_id}/details/   

 

 
#### Error Cases

| Case | Result |
|------|--------|
| `svg_map_url = null` | Field has no map. App should show fallback (e.g., "No layout available"). |
| `background_image_url = null` | Background is optional. App should skip rendering it. |
| 404 on fetch | App should log the issue and ignore that media. |




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
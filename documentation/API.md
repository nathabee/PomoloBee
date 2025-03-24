
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
    - [1 App to Django](#1-app-to-django)
    - [2 Django to ML](#2-django-to-ml)
    - [3 ML to Django](#3-ml-to-django)
  - [Document reference](#document-reference)
  - [**JSON Format Conventions**](#json-format-conventions)
    - [General Rules](#general-rules)
    - [Standard Response Structure](#standard-response-structure)
    - [Standard Error Codes](#standard-error-codes)
    - [Object Field Naming Conventions](#object-field-naming-conventions)
    - [️ Reserved Keys](#reserved-keys)
<!-- TOC END -->
 
</details>

---


## Global API Endpoint Overview

 

### 1 App to Django

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


### 2 Django to ML

| **Category** | **Endpoints** | **Purpose** | **Caller → Receiver** | **Screen Used In** (**Button Triggering API Call**) |
|-------------|--------------|-------------|----------------------|
| **Django → ML API Calls** | `POST /ml/process-image/` | Django sends an image to the ML model for processing. | **Django → ML Model** | **Backend Processing (Automated)** |
| **ML Model Debugging** | `GET /ml/version/` | Fetch the current ML model version. | **Django Backend → ML Model** | **triggered by App GET /api/ml/version/* |


### 3 ML to Django


| **Category** | **Endpoints** | **Purpose** | **Caller → Receiver** | **Screen Used In** (**Button Triggering API Call**) |
|-------------|--------------|-------------|----------------------|
| | `POST /api/images/{image_id}/ml_result` | ML returns detection results to Django. | **ML Model → Django** | **After processing completion** |

 
---
 

## Document reference


-  📖 1 **App to Django** [App to Django specification](API_1_App_to_Django.md)
-  📖 2 **Django to ML** [Django to ML specification](API_2_Django_to_ML.md)
-  📖 3 **ML to Django** [ML to Django specification](API_3_ML_to_Django.md)
 
 

## **JSON Format Conventions**
 

To ensure clarity, consistency, and compatibility across the **PomoloBee ecosystem**, all JSON payloads exchanged between the **App** and the **Django backend** must follow the conventions below.

---

### General Rules

| Rule               | Convention                                           |
|--------------------|------------------------------------------------------|
| **Encoding**        | UTF-8                                                |
| **Key Naming**      | Use `snake_case`                                     |
| **Identifiers**     | Always prefixed: `image_id`, `field_id`, `raw_id`   |
| **Booleans**        | Use JSON booleans: `true` / `false`                 |
| **Numerics**        | Use `integer` or `float` appropriately              |
| **Dates**           | Use ISO 8601 format: `YYYY-MM-DD`                   |
| **Timestamps**      | Use full ISO 8601: `YYYY-MM-DDTHH:MM:SS`            |

✅ **Never expose raw `id` fields** — always use explicit identifiers like `image_id`.

---

### Standard Response Structure

#### Success Responses

```json
{
  "status": "success",
  "data": {
    "image_id": 24,
    "status": "done"
  }
}
```

The `"data"` object contains all returned payload values for a successful request.

---

#### Error Responses

All error responses must follow this structure:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable message (fallback or for logs)"
  }
}
```

✅ **Rules:**
- `code`: machine-readable key used for logic & app i18n
- `message`: developer-readable explanation (or fallback UI)
- `locale_key`: optional key for frontend translation files

---

### Standard Error Codes

| Error Code                | HTTP Status | Description                                                         |
|---------------------------|-------------|---------------------------------------------------------------------|
| `400_BAD_REQUEST`         | 400         | Generic client-side error (invalid JSON, syntax errors, etc.)       |
| `401_UNAUTHORIZED`        | 401         | Auth required or token invalid                                      |
| `403_FORBIDDEN`           | 403         | User is authenticated but lacks permission                         |
| `404_NOT_FOUND`           | 404         | Resource does not exist (e.g. image, history)                       |
| `409_CONFLICT`            | 409         | Conflict with existing resource (e.g. already processed)            |
| `422_UNPROCESSABLE_ENTITY`| 422         | Semantically invalid data (e.g. future date, wrong state)           |
| `500_INTERNAL_ERROR`      | 500         | Internal server error                                               |
| `ML_UNAVAILABLE`          | 503         | ML microservice unreachable                                         |
| `ML_PROCESSING_FAILED`    | 502         | ML failed to process the image and returned an error                |
| `IMAGE_FORMAT_UNSUPPORTED`| 400         | File is not JPEG/PNG                                                |
| `MISSING_PARAMETER`       | 400         | Required parameter(s) not included in the request                   |
| `INVALID_INPUT`           | 400         | One or more fields failed validation (e.g. invalid `raw_id`)        |
| `ALREADY_PROCESSED`       | 409         | Image has already been processed                                    |
| `NO_HISTORY_FOUND`        | 404         | No historical estimation data found                                 |
| `NO_ESTIMATION_FOUND`     | 404         | No yield estimation available for this image                        |
| `RATE_LIMITED` | 429 | If rate-limiting logic is introduced |
| `SERVICE_DEPENDENCY_ERROR` | 503 | For non-ML external services that fail (e.g., object storage) |
| `UPLOAD_FAILED` | 500 | Image upload to storage failed |
| `PAYLOAD_TOO_LARGE` | 413 | (Future-proof) App sends too large file |

---

### Object Field Naming Conventions

| Entity        | Example Keys                        |
|---------------|-------------------------------------|
| **Field**     | `field_id`, `field_name`            |
| **Raw**       | `raw_id`, `nb_plant`, `fruit_type`  |
| **Fruit**     | `fruit_id`, `short_name`            |
| **Image**     | `image_id`, `upload_date`, `status` |
| **Estimation**| `plant_apfel`, `confidence_score`, `raw_kg` |

✅ **Always use** `xxx_id` for references — never just `id`.

---

### ️ Reserved Keys

Avoid using these reserved terms at the top-level of any payload unless specified:

- `id` (always prefix)
- `type`, `object`, `meta`, `links` (reserved for future extensions)

---

# **Django -> ML API Interface Definition**
## **Overview**
This document defines the API interface for the Pomolobee project, specifying:
- API calls and data exchanged
- Endpoints and request/response format
--- 
    
<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [**Django -> ML API Interface Definition**](#django-ml-api-interface-definition)
  - [**Overview**](#overview)
    - [**API Specifications for Django both ML Communication Polling and Error Handling**](#api-specifications-for-django-both-ml-communication-polling-and-error-handling)
    - [**Django to ML Sending Image for Processing**](#django-to-ml-sending-image-for-processing)
    - [**ML Model Debugging**](#ml-model-debugging)
    - [️ ML Endpoint Base Path](#ml-endpoint-base-path)
    - [**Polling Strategy**](#polling-strategy)
  - [**Error Handling Strategy**](#error-handling-strategy)
  - [Basic Contract Tests](#basic-contract-tests)
    - [Recommended Test](#recommended-test)
  - [**JSON Format Conventions**](#json-format-conventions)
<!-- TOC END -->
 
</details>

---

  
 
### **API Specifications for Django both ML Communication Polling and Error Handling**

---

### **Django to ML Sending Image for Processing**
📌 **Purpose:** Django sends an uploaded image to the ML model for apple detection.

✅ **Endpoint:**  
```
POST /ml/process-image/
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
    "image_url": "/media/images/image_24.jpg",
    "image_id": 24
}
```

✅ **Response (Success - 200 OK)**
```json
{
  "status": "success",
  "data": {
    "image_id": 24,
    "message": "Image received, processing started."
  }
}

```

✅ **Response (Error - 400 Bad Request)**
```json
{
  "error": {
    "code": "INVALID_INPUT",
    "message": "Invalid image URL or image ID."
  }
}
or
{
  "error": {
    "code": "MISSING_PARAMETER",
    "message": "Both image_url and image_id are required."
  }
}
or if ML receives a duplicate processing request,
{
  "error": {
    "code": "ALREADY_PROCESSED",
    "message": "This image has already been processed or is in progress."
  }
}


```

--- 
### **ML Model Debugging**
📌 **Purpose:** Fetch the **current ML model version** and status.

✅ **Endpoint:**  
```
GET /ml/version/  
```
✅ **Caller → Receiver:**  
- **Django -> ML**

✅ **Triggered by**: App GET /ml/version/ → Django GET /ml/version/ → ML

✅ **Response (Success - 200 OK)**
```json
{
  "status": "success",
  "data": { 
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

### ️ ML Endpoint Base Path

- All ML endpoints are prefixed with `/ml/` (as defined in `.env`: `ML_API_URL=http://localhost:5000/ml`)
- Example full URL Django will call:
```text
http://localhost:5000/ml/process-image/
{ML_API_URL}/process-image/
```

### **Polling Strategy**
   
📌 If ML takes longer than 5 minutes, Django should **log the delay** and optionally **send a retry request to ML**.  

- To retry processing, Django should simply **re-call** the same endpoint:  
  `POST /ml/process-image/`
- No need for a separate retry-specific endpoint like `/retry-process/`

---

## **Error Handling Strategy**
📌 **What if ML processing fails?**  
- If ML **returns an error**, Django should mark `processed = false` in `ImageHistory`.  
 
 

---

##   Basic Contract Tests

 ### ✅ What Django Expects from ML

- JSON-formatted response
- Responds within 3–5 seconds to acknowledge receipt
- ML service must be reachable at `${ML_API_URL}` (from .env)

### Recommended Test
- Try curl POST to `/ml/process-image/` with a dummy payload
- Verify that Django handles both 200 and 400 responses correctly
-  For ML failure handling tests, see: `tests/test_ml_unavailable.py`


## **JSON Format Conventions**

📌 **IMPORTANT : see documentation  API** [API specification](API.md) defining :
- list of existing error code
- format and naming convention 
- For what triggers ML calls 

---
 
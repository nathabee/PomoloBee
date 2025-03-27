
# **ML -> Django Interface Definition**
## **Overview**
This document defines the API interface for the Pomolobee project, specifying:
- API calls and data exchanged
- Endpoints and request/response format
--- 
   
<details>
<summary>Table of Content</summary>
 
<!-- TOC -->
- [**ML -> Django Interface Definition**](#ml-django-interface-definition)
  - [**Overview**](#overview)
  - [**ML to Django API Endpoint Specifications**](#ml-to-django-api-endpoint-specifications)
    - [**ML to Django Returning Image Processing Results**](#ml-to-django-returning-image-processing-results)
<!-- TOC END -->
 
</details>

---
 
## **ML to Django API Endpoint Specifications**

Here is the **detailed API specification** data, including **purpose, endpoint, request parameters, and response format** based on your Django models.

---



### **ML to Django Returning Image Processing Results**

📌 **Purpose:** The ML model returns **fruit detection results** to Django once image analysis is complete.

✅ **Endpoint:**  

```
POST /api/images/{image_id}/ml_result
```

✅ **Caller → Receiver:**  
- **ML Model → Django Backend**

---

✅ **Path Parameters:**

| **Parameter** | **Type** | **Required?** | **Description** |
|--------------|----------|---------------|-----------------|
| `image_id`   | `integer`| ✅ Yes         | Unique ID of the image being processed |

---

✅ **Request Payload (from ML):**

```json
{
  "nb_fruit": 12,
  "confidence_score": 0.89,
  "processed": true
}




✅ **Response (Success - 200 OK)**
```json
{
  "status": "success",
  "data": {
    "message": "ML result successfully received."
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

 
## **JSON Format Conventions**

📌 **IMPORTANT : see documentation  API** [API specification](API.md) defining :
- list of existing error code
- format and naming convention 
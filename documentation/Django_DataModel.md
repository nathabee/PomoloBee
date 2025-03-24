---
<details>
<summary>Table of Content</summary>

<!-- TOC -->
  - [**Phase 1 MVP Model Structure**](#phase-1-mvp-model-structure)
  - [**Phase 2 User Authentication Access Control**](#phase-2-user-authentication-access-control)
    - [**New Models Ownership Rules**](#new-models-ownership-rules)
    - [**Access Control Strategy**](#access-control-strategy)
    - [**Authorization Rules**](#authorization-rules)
<!-- TOC END -->
 
</details>
---


## **Phase 1 MVP Model Structure**

| Description                                  | Django Model                  |
|----------------------------------------------|-------------------------------|
| Agricultural field                           | `Field(models.Model)`         |
| Tree row (within a field)                    | `Raw(models.Model)`           |
| Fruit type (linked to raw)                   | `Fruit(models.Model)`         |
| Image storage for estimation analysis        | `ImageHistory(models.Model)`  |
| History of raw analysis (processed results)  | `HistoryRaw(models.Model)`    |
| Yield estimation summary                     | `HistoryEstimation(models.Model)` |
| Farm owned by user                           | `Farm(models.Model)`       |

> 🔸 **Note:**  
Although a `Farm` concept exists, the access by farm ID **is not implemented** or used in Phase 1.

---

## **Phase 2 User Authentication Access Control**

### **New Models Ownership Rules**

| Description               | Django Model               |
|---------------------------|----------------------------|
| Registered user (default) | `User` (from `auth.models`)|

---

### **Access Control Strategy**

- Each **User** owns one or multiple **Farms**
- Each **Farm** contains multiple **Fields**
- Each **Field** contains multiple **Raws (tree rows)**

---

### **Authorization Rules**

| Operation                             | Permitted if...                                    |
|---------------------------------------|----------------------------------------------------|
| View fields / raws                    | Field belongs to a farm owned by the user          |
| Upload image for raw                  | Raw belongs to user's farm                         |
| View image or yield estimation result | Image is tied to a raw in one of the user’s farms  |

---
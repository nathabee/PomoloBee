# Django Test Documentation
---
<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [Django Test Documentation](#django-test-documentation)
  - [List of Tests](#list-of-tests)
  - [️ Migration Tests](#migration-tests)
    - [Purpose](#purpose)
    - [Fixtures Required](#fixtures-required)
    - [Classes Coverage](#classes-coverage)
  - [Endpoint Tests](#endpoint-tests)
  - [Workflow Tests](#workflow-tests)
    - [Prerequisites](#prerequisites)
    - [Test Coverage Mapping](#test-coverage-mapping)
  - [️ ML Unavailable Tests](#ml-unavailable-tests)
  - [Django both ML Communication Tests](#django-both-ml-communication-tests)
<!-- TOC END -->
 
</details>
---
 

## List of Tests

| 📄 **File**                       | ✅ **Coverage**                                                                                     |
|----------------------------------|-----------------------------------------------------------------------------------------------------|
| `tests.py`                       | Root file — executes all test files under `core.tests`                                              |
| `test_migration.py`              | Validates that **fixtures are loaded correctly** and **models are initialized**                     |
| `test_endpoint.py`               | Tests **API endpoints** against [API specs](API.md), [App↔Django](API_1_App_to_Django.md), [ML↔Django](API_3_ML_to_Django.md) |
| `test_ml_unavailable.py`         | Tests how Django reacts when **ML service is unreachable**                                          |
| `test_workflow.py`               | Validates the **entire image estimation workflow** as defined in `Django_Specification.md`          |
| `test_ml_response.py`            | Validates **Django ↔ ML** communication: request/response logic from [API_2_Django_to_ML.md](API_2_Django_to_ML.md) |

---

## ️ Migration Tests

### Purpose
Ensure that your **initial fixtures** (JSON) load successfully and all key **model tables** are present and behaving correctly.

### Fixtures Required
Stored in `core/fixtures/`

```
initial_superuser.json
initial_farms.json
initial_fields.json
initial_fruits.json
initial_raws.json
```

### Classes Coverage

| **Class**                        | **Test Name**                            | **Coverage / What it Verifies**                                     |
|----------------------------------|------------------------------------------|----------------------------------------------------------------------|
| `LoadFixtureDataTest`           | `test_superuser_exists`                  | Superuser (`pomobee`) exists from fixture                           |
|                                  | `test_farms_count`                       | At least one `Farm` loaded                                           |
|                                  | `test_field_count`, `test_fruit_count`, `test_raw_count` | Expected number of objects from fixtures                         |
|                                  | `test_specific_field_data`, `test_specific_fruit_data`, `test_specific_raw_data` | Validates key values of selected rows |
| `ModelTableExistenceTest`       | Tests for `ImageHistory`, `HistoryRaw`, `HistoryEstimation` | Can insert and retrieve expected models                           |
| `AutoHistoryCreationTest`       | `test_history_raw_created_after_ml_result` | Signal triggers creation of `HistoryRaw` and `HistoryEstimation`    |

---



## Endpoint Tests

See [API.md](API.md) for full spec.

Tested in: `core/tests/test_endpoint.py`

| Endpoint                        | Status | Notes                              |
|---------------------------------|--------|------------------------------------|
| `GET /api/fields/`              | ✅     | Returns all fields                 |
| `GET /api/fruits/`              | ✅     | Returns all fruits                 |
| `GET /api/locations/`           | ✅     | Combines fields and raws           |
| `POST /api/images/`             | ✅     | Uploads image                      |
| `GET /api/images/<id>/status/` | ✅     | Polls processing state             |
| `GET /api/estimations/<id>/`   | ✅     | Returns estimation data            |
| `DELETE /api/images/<id>/`     | ✅     | Deletes image                      |
| `POST /api/retry_processing/`  | ✅     | Re-triggers ML                     |
| `GET /api/ml/version/`         | ✅     | ML model version health check      |

 ### 📦 Prerequisites

- Fixtures loaded during test 
- Existing image:  - `media/images/orchard.jpg`
- start django server to have acess to http://localhost:8000/media/images/image_15.jpg

  ```bash
  cd PomoloBeeDjango
  source venv/bin/activate
  python manage.py runserver

  ```
- ML service running:
  ```bash
  cd PomoloBeeML
  source venv/bin/activate
  python app.py
  ```


## Workflow Tests

Defined in [`Workflow.md`](Workflow.md). These tests simulate an end-to-end scenario: uploading an image, triggering ML, storing results, and retrieving estimations.

### Prerequisites

- Fixtures loaded during test
- Existing image:  - `media/images/orchard.jpg`
- ML service running:
  ```bash
  cd PomoloBeeML
  source venv/bin/activate
  python app.py
  ```

### Test Coverage Mapping

| **Diagram Line**                                          | **Test Implemented?** | **Test(s)**                                              |
|-----------------------------------------------------------|------------------------|----------------------------------------------------------|
| App → Django `"📍 Fetch Available Fields & Raws"`         | ✅                     | `GET /api/fields/`, `GET /api/locations/`                |
| Django → DB `"📂 Save Image Metadata"`                    | ✅                     | `POST /api/images/` creates `ImageHistory`               |
| Django → App `"📄 Provide Field & Raw Data"`              | ✅                     | `GET /api/locations/` returns correct data               |
| Django → ML `"🔄 Send Image to ML"`                       | ✅                     | Internal ML API call tested via `requests.post(...)`     |
| ML → Django `"📊 Return Detection Results"`               | ✅                     | `POST /api/images/{image_id}/ml_result`                  |
| Django → App `"📥 Fetch Processing Status"`               | ✅                     | `GET /api/images/{image_id}/ml_result` or `/status/`     |
| Django → App `"📥 Fetch Estimation Results"`              | ✅                     | `GET /api/estimations/{image_id}/`                       |

---

## ️ ML Unavailable Tests

Located in: `test_ml_unavailable.py`

- Covers behavior when ML service is down, timeout, or unreachable.
- Validates proper error logging and safe fallback from Django.

---

## Django both ML Communication Tests

Located in: `test_ml_response.py`

Covers:
- Django successfully sending images to `/ml/process-image`
- Django receiving correct format from `/api/images/<id>/ml_result`
- Edge case validation: missing fields, invalid confidence scores, etc.

---
 
# Django Test Documentation
---

<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [Django Test Documentation](#django-test-documentation)
  - [List of Tests](#list-of-tests)
  - [Unit Tests Migration](#unit-tests-migration)
    - [Purpose](#purpose)
    - [Fixtures Required](#fixtures-required)
    - [Test Coverage](#test-coverage)
  - [Unit Tests API Endpoint Coverage](#unit-tests-api-endpoint-coverage)
    - [Prerequisites](#prerequisites)
  - [Integration Non-Regression Tests](#integration-non-regression-tests)
    - [Purpose](#purpose)
    - [What it Tests](#what-it-tests)
    - [Modes](#modes)
    - [Features](#features)
    - [How to Run](#how-to-run)
    - [Prerequisites](#prerequisites)
  - [Test in case of installation to check installation](#test-in-case-of-installation-to-check-installation)
<!-- TOC END -->

</details>

---

## List of Tests

| 📄 **File**                  | ✅ **Coverage**                                                                                                               |
|-----------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `integration_test.sh`   | End-to-end integration + non-regression tests (App ↔ Django ↔ ML) against [API.md](API.md), [Workflow.md](Workflow.md)       |
| `installation_test.sh`   | ECall all unit test with ML in correct mok mode   |                                                          |
| `test_migration.py`         | Validates that fixtures load correctly and that base models are present                                                       |
| `test_ml_unavailable.py`   | Check correct behaviour id ML not started                                                      |
| `test_ml_response.py`       | Check correct behaviour id ML POST to Django                                                    |
| `test_endpoint.py`          | Unit tests for each API endpoint using mocked serializers and models                                                 |
| `test_workflow.py`          | Unit tests for workflow unit test                                                     |

---

## Unit Tests Migration

### Purpose
Ensure that your **fixtures load correctly** and that the database contains all required objects and relationships.

### Fixtures Required
Located in `core/fixtures/`:
```bash
initial_superuser.json
initial_farms.json
initial_fields.json
initial_fruits.json
initial_rows.json
```

### Test Coverage

| **Class**                | **Test**                                 | **What It Verifies**                                             |
|--------------------------|-------------------------------------------|------------------------------------------------------------------|
| `LoadFixtureDataTest`   | `test_superuser_exists`                  | Superuser (`pomobee`) created correctly                         |
|                          | `test_farms_count`, `test_field_count`, `test_fruit_count`, `test_row_count` | Correct number of entries from fixtures          |
|                          | `test_specific_*`                        | Specific field, fruit, and row content                         |
| `ModelTableExistenceTest`|  *[Various]*                             | Tables like `ImageHistory`, `HistoryEstimation` exist and work  |
| `AutoHistoryCreationTest`| `test_history_row_created_after_ml_result` | Auto-history creation via signal logic                          |

---

## Unit Tests API Endpoint Coverage

Tested in: `core/tests/test_endpoint.py`  
Reference: [API.md](API.md)

| Endpoint                          | Status | Description                          |
|-----------------------------------|--------|--------------------------------------|
| `GET /api/fields/`                | ✅     | Fetches all fields                   |
| `GET /api/fruits/`                | ✅     | Fetches all fruit types              |
| `GET /api/locations/`             | ✅     | Returns fields with nested tree rows |
| `POST /api/images/`              | ✅     | Uploads image + metadata             |
| `GET /api/images/<id>/status/`   | ✅     | Polls processing state               |
| `GET /api/estimations/<id>/`     | ✅     | Returns processed estimation         |
| `DELETE /api/images/<id>/`       | ✅     | Deletes image                        |
| `POST /api/retry_processing/`    | ✅     | Re-triggers ML inference             |
| `GET /api/ml/version/`           | ✅     | ML backend version info              |

### Prerequisites

- Django running:  
  ```bash
  python manage.py runserver
  ```

- Required image:  
  `media/images/orchard.jpg`

- ML service running (or mocked):  
  ```bash
  cd PomoloBeeML
  source venv/bin/activate
  python app.py mok_short
  ```



## Integration Non-Regression Tests

📁 File: `tests/integration_test.sh`

### Purpose
Performs a **complete system-level test** by executing real API calls (as the app would), validating responses against snapshots, and detecting regressions.

### What it Tests

- End-to-end API flow from **image upload to estimation retrieval**
- **ML interaction** (request and result callback)
- Snapshot-based **regression detection**
- Image **deletion flow** and behavior on **invalid data**
- All endpoints defined in App↔Django and ML↔Django specs

### Modes

| Flag          | Description                                            |
|---------------|--------------------------------------------------------|
| `--snapshot`  | Generates new snapshots from current backend behavior  |
| `--nonreg`    | Compares live API responses to stored snapshots        |
| `--integ`     | Basic print-only integration run (no snapshot checking)|

### Features

- 🧪 Tests **real Django server behavior** via `curl` calls
- 🧠 Calls ML as Django would (`requests.post(...)`)
- 📥 Accepts simulated ML result with `POST /ml_result/`
- ✅ Snapshots are **normalized** to ignore volatile fields
- 💥 Detects API changes, broken estimations, or silent regressions

### How to Run

```bash
# Integration test call all API and trace them on screen
# Format must be validated against API specification
./tests/integration_test.sh --integ


# Generate new expected snapshots
./tests/integration_test.sh --snapshot

# Validate that no API behavior changed
./tests/integration_test.sh --nonreg
```

### Prerequisites

Make sure the following conditions are met before running the integration test script:

- ✅ Fixtures are loaded (`manage.py loaddata`) so that fields, fruits, rows exist
- ✅ A test image exists at `media/images/orchard.jpg`
- ✅ Django is running at `http://127.0.0.1:8000` so API calls work
- ✅ The `/media/` URL is accessible for image serving

```bash
cd PomoloBeeDjango
source venv/bin/activate
python manage.py runserver
```

✅ The ML mock service is running to receive image processing requests:

```bash
cd PomoloBeeML
source venv/bin/activate
python app.py mok
```
--- 
## Test in case of installation to check installation

all units tests nd non regression tests are called with 

📁 File: `tests/installation_test.sh`

```bash
cd PomoloBeeDjango
source venv/bin/activate
python manage.py runserver
./tests/installation_test.sh
```

---

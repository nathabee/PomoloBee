Here's your **ML Test Documentation**, cleaned up and structured nicely for in-chat reading — with formatting fixes, better flow, and full clarity:

---

# ML Test Documentation

---

<details>
<summary><strong>📚 Table of Contents</strong></summary>

- [ML Mock and Debug Modes](#ml-mock-and-debug)
- [Test Modes Summary](#test-modes-summary)
- [Start Django Unit Test](#start-django-unit-test)
- [List of Unit Tests](#list-of-unit-tests)
- [Start Django Integration Test](#start-django-integration-test)
- [Start Django Validation Test](#start-django-validation-test)
- [Start Django in Normal (Production) Mode](#start-django-normal-mode-production-no-test)

</details>

---
## Test API Django<->ML


### ️ ML Mock and Debug

The Flask-based ML service supports different modes controlled by `flask_config.json`.

The mocking tool is used to simulate ML behavior, depending on whether you're doing unit tests, integration tests, or full validation.

---

### Test Modes Summary

| **Use Case**           | `MOK` | `MOK_RUNSERVER` | 🧠 **Behavior**                                                        |
|------------------------|-------|------------------|------------------------------------------------------------------------|
| Django Unit Tests      | ✅     | ❌               | Return `MOK_RETURN` + `MOK_CODE` immediately — no image or POST        |
| Django Integration     | ✅     | ✅               | Queue image, simulate background POST to Django after `MOK_DELAY`      |
| Full Validation / Prod | ❌     | ❌               | Use real image detection (`cv2`) + ML logic                            |

---

### ▶️ Start Django Unit Test

Used for isolated testing without real image processing or Django being online.

#### `flask_config.json`
```json
{
    "DEBUG": true,
    "ML_MODEL_VERSION": "v1.2.5",
    "LAST_UPDATED": "2024-03-10T14:00:00",
    "DJANGO_API_URL": "http://localhost:8000/api",
    "LOG_FILE": "logs/flask.log",
    "MOK": true,
    "MOK_RUNSERVER": false,
    "MOK_CODE": 200,
    "MOK_RETURN": { "message": "Mock OK" }
}
```

#### Run
```bash
# ML Mock Server
cd pomoloBeeML
source venv/bin/activate
python app.py
```

```bash
# Django Unit Tests
cd pomoloBeeDjango
source venv/bin/activate
python manage.py test core.tests.test_endpoint
```

---

### List of Unit Tests

| 📄 **File**              | ✅ **Coverage**                                                       |
|--------------------------|----------------------------------------------------------------------|
| `test_endpoint.py`       | Test Django API endpoints (GET, POST, DELETE)                        |
| `test_workflow.py`       | Full ML workflow (upload image → process → get result)              |
| `test_ml_response.py`    | Communication between Django → ML and ML → Django                   |
| `test_ml_unavailable.py` | Handles failure or offline ML server                                |

---

### Start Django Integration Test

Used to simulate full background processing with delayed POST to Django.
            |

#### `flask_config.json`
```json
{
    "DEBUG": true,
    "ML_MODEL_VERSION": "v1.2.5",
    "LAST_UPDATED": "2024-03-10T14:00:00",
    "DJANGO_API_URL": "http://localhost:8000/api",
    "LOG_FILE": "logs/flask.log",
    "MOK": true,
    "MOK_RUNSERVER": true,
    "MOK_CODE": 200,
    "MOK_RETURN": { "message": "Mock OK" },
    "MOK_DELAY": 3,
    "MOK_MLRESULT": {
        "nb_fruit": 10,
        "confidence_score": 0.85,
        "processed": true
    }
}
```

#### Run

note : for the test core.tests.ml_unavailable : we will NOT start app.py
```bash
# ML Mock Server
cd pomoloBeeML
source venv/bin/activate
python app.py
```

```bash
# Django App Server
cd pomoloBeeDjango
source venv/bin/activate
python manage.py runserver
```

---



### Start Django Validation Test

Used to run against the **real ML logic** — no mocking.

#### `flask_config.json`
```json
{
    "DEBUG": true,
    "ML_MODEL_VERSION": "v1.2.5",
    "LAST_UPDATED": "2024-03-10T14:00:00",
    "DJANGO_API_URL": "http://localhost:8000/api",
    "LOG_FILE": "logs/flask.log",
    "MOK": false
}
```

#### Run
```bash
# ML Engine real
cd pomoloBeeML
source venv/bin/activate
python app.py
```

```bash
# Django App
cd pomoloBeeDjango
source venv/bin/activate
python manage.py runserver
```

---

### Start Django Normal Mode Production No Test

In production, you should use:

- `MOK = false`
- `DEBUG = false`

Use production servers like **Gunicorn** or **uWSGI** to launch.

---
## Test ML

Absolutely! Here's your second chapter — **Unit Testing ML** — nicely formatted and consistent with the first section of your ML Test Documentation.

---

## ML Unit Test Documentation

---

### Overview

This section focuses on testing the ML Flask server **independently of Django**, using direct `curl` or Python requests to the endpoints.

You will:

- Test `/ml/version`
- Test `/ml/process-image` with and without actual image
- Simulate failure and retry logic
- Optionally test `detect_fruit()` manually
- Check mock behavior based on config

---

### Unit Test Get ML Version

#### Goal
Verify that the ML server responds correctly with its metadata.

#### Required

Ensure `app.py` is running (mock or real):

```bash
cd pomoloBeeML
source venv/bin/activate
python app.py
```

#### Command

```bash
curl -X GET http://localhost:5000/ml/version
```

#### Expected Output

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

---

### Unit Test Process Image Success Case

#### Goal
Simulate sending an image to `/ml/process-image` using `image_id` and `image_url`.

#### Precondition

The image must be reachable from the URL. You can use Django static server or host it locally.

Example URL:  
`http://localhost:8000/media/images/image-125.jpg`

#### `flask_config.json` for real test

```json
{
  "DEBUG": true,
  "MOK": false
}
```

#### Command

```bash
curl -X POST http://localhost:5000/ml/process-image \
  -H "Content-Type: application/json" \
  -d '{"image_id": "125", "image_url": "http://localhost:8000/media/images/image-125.jpg"}'
```

#### Expected Output

```json
{
  "status": "success",
  "data": {
    "message": "Image 125 received, processing started."
  }
}
```

You should then see background logs in Flask and a simulated POST back to Django (if MOK_RUNSERVER = true).

---

### Unit Test Retry Processing Failed Image

#### Goal
Simulate retrying an image that initially failed.

#### Steps

1. **Send an invalid image** to trigger failure:

```bash
curl -X POST http://localhost:5000/ml/process-image \
  -H "Content-Type: application/json" \
  -d '{"image_id": "fail_test", "image_url": "http://localhost:8000/media/images/not_found.jpg"}'
```

2. **Fix the image issue** (e.g., place a real image at that URL).

3. **Retry the same ID**:

```bash
curl -X POST http://localhost:5000/ml/process-image \
  -H "Content-Type: application/json" \
  -d '{"image_id": "fail_test", "image_url": "http://localhost:8000/media/images/image-fixed.jpg"}'
```

#### Output

You should see a retry message, then successful result.

---

### Unit Test Mock Mode Shortcut No Server

#### Goal
Use Flask mock server without running Django. Only mock result is returned — no processing, no POST.

#### `flask_config.json`

```json
{
  "DEBUG": true,
  "MOK": true,
  "MOK_RUNSERVER": false,
  "MOK_CODE": 200,
  "MOK_RETURN": { "message": "Mock OK" }
}
```

#### Command

```bash
curl -X POST http://localhost:5000/ml/process-image \
  -H "Content-Type: application/json" \
  -d '{"image_id": "mockunit", "image_url": "http://localhost:8000/media/images/anything.jpg"}'
```

#### Output

```json
{
  "message": "Mock OK"
}
```

Even if image doesn't exist — no download or POST happens.

---

### Manual Test Run Detection on Image

If you want to test the internal function manually:

```python
from app import detect_fruit
nb, confidence = detect_fruit("uploads/image_125.jpg")
print(nb, confidence)
```

---

### Evaluate a Trained Model

If you're using YOLOv8 or similar:

```python
metrics = model.val()
print(metrics)
```

Metrics might include:
- mAP (mean average precision)
- precision / recall
- confusion matrix

---

### Unit Test Sample Image Run

```python
results = model('data/images/val/sample.jpg')
results.show()   # Show predictions
results.save()   # Save results to disk
```

---

### Integration Test Sample Workflow

1. Start Flask (with MOK or real mode)
2. Upload image via Django `/api/images/`
3. Observe ML → Django result post
4. Fetch estimation `/api/images/{id}/estimations`

---
 
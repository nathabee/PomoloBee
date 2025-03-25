import json
import threading
from flask import Flask, request, jsonify
import os, time, cv2, numpy as np, requests
import logging


app = Flask(__name__)



# ------------------------------
# ✅ CONFIG: Load from JSON FILE
# ------------------------------
CONFIG_PATH = "flask_config.json"
default_config = {
    "DEBUG": False,
    "MOK": False,
    "MOK_CODE": 200,
    "MOK_RETURN": {"message": "Default mock"},
    "MOK_DELAY": 3,
    "MOK_MLRESULT": {
        "nb_apples": 10,
        "confidence_score": 0.85,
        "processed": True
    }
}
try:
    with open(CONFIG_PATH) as f:
        FLASK_CONFIG = {**default_config, **json.load(f)}
except Exception as e:
    logging.debug("⚠️ Could not load config, using defaults.")
    FLASK_CONFIG = default_config
 

IS_DEBUG_MODE = FLASK_CONFIG.get("DEBUG", False)
IS_MOCK_MODE = FLASK_CONFIG.get("MOK", False)


# ------------------------------
# ✅ LOG files
# ------------------------------
 
if IS_DEBUG_MODE:
    log_path = FLASK_CONFIG.get("LOG_FILE", "flask_debug.log")
    os.makedirs(os.path.dirname(log_path), exist_ok=True)
    logging.basicConfig(filename=log_path, level=logging.DEBUG, format='%(asctime)s %(message)s')
else:
    logging.basicConfig(level=logging.CRITICAL)  # suppress logs in prod

 
 



# ------------------------------
# ML & STORAGE CONFIG
# ------------------------------
UPLOAD_FOLDER = os.path.join(os.getcwd(), "uploads")
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

DJANGO_API_URL = FLASK_CONFIG.get("DJANGO_API_URL", "http://django-backend:8000/api/images/")
ML_MODEL_VERSION = FLASK_CONFIG.get("ML_MODEL_VERSION", "v1.0.0")
LAST_UPDATED = FLASK_CONFIG.get("LAST_UPDATED", "2024-01-01T00:00:00")

processing_queue = {}

# ------------------------------
# 🧠 ML SIMULATION
# ------------------------------
def detect_apples(image_path):
    img = cv2.imread(image_path)
    if img is None:
        return None, None
    nb_apples = np.random.randint(5, 20)
    confidence_score = round(np.random.uniform(0.7, 0.95), 2)
    return nb_apples, confidence_score

# ------------------------------
# 🔁 API HELPERS
# ------------------------------
def api_error(code, message, status_code=400):
    return jsonify({"error": {"code": code, "message": message}}), status_code

def api_success(data, status_code=200):
    return jsonify({"status": "success", "data": data}), status_code

# ------------------------------
# 🔁 POST /ml/process-image
# ------------------------------
@app.route('/ml/process-image', methods=['POST'])
def process_image():
    data = request.json
    image_id = data.get("image_id")
    image_url = data.get("image_url")

    if not image_id or not image_url:
        return api_error("INVALID_INPUT", "Invalid image URL or image ID.", 400)

   

    # ✅ Safety Check for Idempotency and Retry
    if image_id in processing_queue:
        status = processing_queue[image_id]["status"]

        if status == "completed":
            return api_error("ALREADY_PROCESSED", f"Image {image_id} was already processed.", 409)

        elif status == "failed":
            logging.debug(f"🔁 Retrying failed image {image_id}...")
            processing_queue[image_id]["status"] = "processing"  # requeue

        elif status in ("processing","moking"):
            return api_success({
                "message": f"Image {image_id} is already queued for processing."
            })


    # ✅ Real Mode: Download image
    image_path = os.path.join(UPLOAD_FOLDER, f"image_{image_id}.jpg")

    try:
        response = requests.get(image_url, stream=True)
        if response.status_code == 200:
            with open(image_path, 'wb') as f:
                for chunk in response.iter_content(1024):
                    f.write(chunk)
        else:
            return api_error("ML_PROCESSING_FAILED", "Failed to download image.", 502)

    except requests.RequestException as e:
        return api_error("500_INTERNAL_ERROR", str(e), 500)
    
    if IS_MOCK_MODE:
        logging.debug("🧪 MOK MODE ENABLED. Queuing mock image for background simulation.")
        status = "moking"
        image_path = "mock.jpg"
    else:
        status = "processing"

    processing_queue[image_id] = {
        "image_path": image_path,
        "status": status,
        "queued_at": time.time()
    }

    # If mock code is meant to fail immediately
    if IS_MOCK_MODE and FLASK_CONFIG["MOK_CODE"] >= 300:
        return api_error("MOCK_ERROR", FLASK_CONFIG["MOK_RETURN"].get("message", "Mock error"), FLASK_CONFIG["MOK_CODE"])



    return api_success({
        "message": f"Image {image_id} received, processing started."
    })


# ------------------------------
# ⏳ Background ML Processing
# ------------------------------
def background_process_images():
    while True:
        time.sleep(1)

        for image_id, data in list(processing_queue.items()):
            status = data["status"]

            # Handle mock mode simulation (only when enough time passed)
            if status == "moking" and IS_MOCK_MODE:
                delay = FLASK_CONFIG.get("MOK_DELAY", 1)
                elapsed = time.time() - data.get("queued_at", 0)

                if elapsed < delay:
                    continue  # not ready yet

                logging.debug(f"🧪 MOK DONE: Sending fake ML result for image {image_id}")
                payload = {
                    "image_id": image_id,
                    **FLASK_CONFIG.get("MOK_MLRESULT", {})
                }
                nb_apples = payload.get("nb_apples") 

            elif status == "processing":
                logging.debug(f"🔄 Processing image {image_id}...")
                nb_apples, confidence_score = detect_apples(data["image_path"])
                if nb_apples is None:
                    processing_queue[image_id]["status"] = "failed"
                    logging.debug(f"❌ Failed to read image {image_id}")
                    continue
                payload = {
                    "image_id": image_id,
                    "nb_apples": nb_apples,
                    "confidence_score": confidence_score,
                    "processed": True
                }

            else:
                continue  # skip unknown or completed/failed statuses

            # Try to POST to Django
            try:
                res = requests.post(f"{DJANGO_API_URL}/images/{image_id}/ml_result/", json=payload)
                if res.status_code == 200:
                    processing_queue[image_id]["status"] = "completed"
                    logging.debug(f"✅ ML Result sent for {image_id}")
                else:
                    processing_queue[image_id]["status"] = "failed"
                    logging.debug(f"❌ Django rejected ML result: {res.status_code} for {image_id}")
            except Exception as e:
                processing_queue[image_id]["status"] = "failed"
                logging.debug(f"❌ Exception posting to Django: {e}")

# ------------------------------
# /ml/version
# ------------------------------
@app.route('/ml/version', methods=['GET'])
def get_version():
    return api_success({
        "model_version": ML_MODEL_VERSION,
        "status": "active",
        "last_updated": LAST_UPDATED
    })

# ------------------------------
# Run Flask
# ------------------------------
if __name__ == '__main__':
    t = threading.Thread(target=background_process_images, daemon=True)
    t.start()
    app.run(host="0.0.0.0", port=5000, debug=FLASK_CONFIG["DEBUG"])

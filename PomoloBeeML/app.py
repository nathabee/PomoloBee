import json
import threading
from flask import Flask, request, jsonify
import os, time, cv2
import numpy as np, requests
import logging
import sys 



app = Flask(__name__)



# ------------------------------
# ✅ CONFIG: Load from JSON FILE
# ------------------------------
# ---------------------
# Step 1: Get argument
# ---------------------
if len(sys.argv) < 2:
    print("❌ Usage: python app.py <config_name>")
    sys.exit(1)

config_name = sys.argv[1]
config_path = os.path.join("config", f"{config_name}.json")

# ---------------------
# Step 2: Load config
# ---------------------
if not os.path.exists(config_path):
    print(f"❌ Config file not found: {config_path}")
    sys.exit(1)

with open(config_path, "r") as f:
    try:
        FLASK_CONFIG = json.load(f)
    except json.JSONDecodeError as e:
        print(f"❌ Invalid JSON: {e}")
        sys.exit(1)

# ---------------------
# Step 3: Validate config
# ---------------------
REQUIRED_KEYS = [
    "DEBUG",
    "UPLOAD_FOLDER",
    "ML_MODEL_VERSION",
    "LAST_UPDATED",
    "DJANGO_API_URL",
    "DJANGO_MEDIA_URL",
    "LOG_FILE",
    "MOK",
    "MOK_RUNSERVER"
]

missing_keys = [k for k in REQUIRED_KEYS if k not in FLASK_CONFIG]
if missing_keys:
    print(f"❌ Missing required config keys: {', '.join(missing_keys)}")
    logging.debug(f"❌ Missing required config keys: {', '.join(missing_keys)}")
    sys.exit(1)

 

 

# ------------------------------
# ✅ LOG files
# ------------------------------
 
if FLASK_CONFIG["DEBUG"] :
    log_path = FLASK_CONFIG["LOG_FILE"] 
    os.makedirs(os.path.dirname(log_path), exist_ok=True)

    logging.basicConfig(
        level=logging.DEBUG,
        format='%(asctime)s %(message)s',
        handlers=[
            logging.FileHandler(log_path),
            logging.StreamHandler()  # <- Also output to console
        ]
    )
else:
    logging.basicConfig(level=logging.CRITICAL)  # suppress logs in prod




# ------------------------------
# ML & STORAGE CONFIG
# ------------------------------
UPLOAD_FOLDER = os.path.abspath(FLASK_CONFIG["UPLOAD_FOLDER"])
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

logging.debug(f"📁 Saving image to: {UPLOAD_FOLDER}")

processing_queue = {}

# ------------------------------
# 🧠 ML SIMULATION
# ------------------------------
def detect_fruit(image_path):
    img = cv2.imread(image_path)
    if img is None:
        return None, None
    fruit_plant = np.random.randint(5, 20)
    confidence_score = round(np.random.uniform(0.7, 0.95), 2)
    return fruit_plant, confidence_score

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

    # If this is NOT a relative path, it’s a bug!
    if not image_id or not image_url:
        logging.debug(f"INVALID_INPUT Invalid image URL or image ID. error 400")
        return api_error("INVALID_INPUT", "Invalid image URL or image ID.", 400)
 
    if not image_url.startswith("/media/"):
        logging.debug(f"❌ REJECTED: image_url must be relative, got: {image_url}")
        return api_error("INVALID_PATH", "image_url must be a relative /media path.", 400)


    # Always prepend DJANGO_MEDIA_URL
    image_url = FLASK_CONFIG["DJANGO_MEDIA_URL"].rstrip("/") + image_url

    logging.debug(f"📥 Trying to download image from: {image_url}")


    if not image_id or not image_url:
        logging.debug(f"INVALID_INPUT Invalid image URL or image ID. error 400")
        return api_error("INVALID_INPUT", "Invalid image URL or image ID.", 400)

    # ➕ Handle short-circuit mock mode when MOK_RUNSERVER == False
    if FLASK_CONFIG["MOK"]:
        if not FLASK_CONFIG["MOK_RUNSERVER"]:
            logging.debug(f"🧪 MOK_SHORT: Sending immediate mock response for image {image_id}")
            
            if FLASK_CONFIG["MOK_CODE"] == 200:
                # ✅ Simulate a proper success response structure
                return api_success({
                    "image_id": image_id,
                    "message": "Mocked image received, processing started."
                })
            else:
                return api_error("MOK_ERROR", FLASK_CONFIG["MOK_RETURN"].get("message", "Mock error"), FLASK_CONFIG["MOK_CODE"])


    # ✅ Safety check for already queued
    if image_id in processing_queue:
        status = processing_queue[image_id]["status"]
        if status == "completed":
            return api_error("ALREADY_PROCESSED", f"Image {image_id} was already processed.", 409)
        elif status == "failed":
            logging.debug(f"🔁 Retrying failed image {image_id}")
            processing_queue[image_id]["status"] = "processing"
        elif status in ("processing", "moking"): 
                return api_success({
                    "image_id": image_id,
                    "message": "Image is already queued for processing.."
                 })


    # ✅ Real Mode: Download image (for full mock + real processing)
    image_path = os.path.join(UPLOAD_FOLDER, f"image_{image_id}.jpg")
    logging.debug(f"Trying to access image at: {image_path}")

    try:
        logging.debug(f"Trying to access image from: {image_url}")
        response = requests.get(image_url, stream=True)
        if response.status_code == 200:
            with open(image_path, 'wb') as f:
                for chunk in response.iter_content(1024):
                    f.write(chunk)
        else:
            logging.debug(f"ML_PROCESSING_FAILED Failed to download image. error 502")
            return api_error("ML_PROCESSING_FAILED", "Failed to download image.", 502)
    except requests.RequestException as e:
        logging.debug(f"500_INTERNAL_ERROR {str(e)} 500")
        return api_error("500_INTERNAL_ERROR", str(e), 500)

    # ✅ Decide processing mode
    if FLASK_CONFIG["MOK"]:
        status = "moking" 
    else:
        status = "processing"

    processing_queue[image_id] = {
        "image_path": image_path,
        "status": status,
        "queued_at": time.time()
    }


    return api_success({
            "image_id": image_id,
            "message": "Image received, processing started."
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
            if status == "moking" and FLASK_CONFIG["MOK"]:
                delay = FLASK_CONFIG.get("MOK_DELAY", 1)
                elapsed = time.time() - data.get("queued_at", 0)

                if elapsed < delay:
                    continue  # not ready yet

                logging.debug(f"🧪 MOK DONE: Sending fake ML result for image {image_id}")
                payload = {
                    "image_id": image_id,
                    **FLASK_CONFIG.get("MOK_MLRESULT", {})
                }
                fruit_plant = payload.get("fruit_plant") 

            elif status == "processing":
                logging.debug(f"🔄 Processing image {image_id}...")
                fruit_plant, confidence_score = detect_fruit(data["image_path"])
                if fruit_plant is None:
                    processing_queue[image_id]["status"] = "failed"
                    logging.debug(f"❌ Failed to read image {image_id}")
                    continue
                payload = {
                    "image_id": image_id,
                    "fruit_plant": fruit_plant,
                    "confidence_score": confidence_score,
                    "processed": True
                }

            else:
                continue  # skip unknown or completed/failed statuses

            # Try to POST to Django
            try:
                res = requests.post(f"{FLASK_CONFIG['DJANGO_API_URL']}/api/images/{image_id}/ml_result/", json=payload)
                if res.status_code == 200:
                    processing_queue[image_id]["status"] = "completed"
                    logging.debug(f"✅ ML Result sent for {image_id}")
                else:
                    processing_queue[image_id]["status"] = "failed"
                    logging.debug(f"❌ Django rejected ML result: {res.status_code} for {image_id} for payload {payload}")
            except Exception as e:
                processing_queue[image_id]["status"] = "failed"
                logging.debug(f"❌ Exception posting to Django: {e}")

# ------------------------------
# /ml/version
# ------------------------------
@app.route('/ml/version', methods=['GET'])
def get_version():
    return api_success({
        "model_version": FLASK_CONFIG["ML_MODEL_VERSION"],
        "status": "active",
        "last_updated": FLASK_CONFIG["LAST_UPDATED"]
    })

# ------------------------------
# Run Flask
# ------------------------------
if __name__ == '__main__':
    t = threading.Thread(target=background_process_images, daemon=True)
    t.start()
    app.run(host="0.0.0.0", port=5000, debug=FLASK_CONFIG["DEBUG"])

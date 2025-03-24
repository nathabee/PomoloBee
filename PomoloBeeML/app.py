from flask import Flask, request, jsonify
import os
import time
import cv2
import numpy as np
import requests

app = Flask(__name__)

# Ensure upload folder exists
UPLOAD_FOLDER = "uploads"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Django API Endpoint to POST results
DJANGO_API_URL = os.getenv("DJANGO_API_URL", "http://django-backend:8000/api/images/")

# ML Model Version
ML_MODEL_VERSION = "v1.2.5"
LAST_UPDATED = "2024-03-10T14:00:00"

# Dummy Storage for Processed Results
processing_queue = {}


def api_error(code, message, status_code=400):
    return jsonify({
        "error": {
            "code": code,
            "message": message
        }
    }), status_code

def api_success(data, status_code=200):
    return jsonify({
        "status": "success",
        "data": data
    }), status_code


def detect_apples(image_path):
    """Simulated Apple Detection (Replace with real ML model later)."""
    img = cv2.imread(image_path)
    if img is None:
        return None, None  # Image could not be read

    nb_apples = np.random.randint(5, 20)  # Random apple count
    confidence_score = round(np.random.uniform(0.7, 0.95), 2)  # Random confidence score

    return nb_apples, confidence_score


@app.route('/ml/process-image', methods=['POST'])
def process_image():
    """Receives an image for processing, returns 200 OK immediately, then processes asynchronously."""
    data = request.json
    image_id = data.get("image_id")
    image_url = data.get("image_url")

    if not image_id or not image_url:
        #return jsonify({"error": "Invalid image URL or image ID."}), 400
        return api_error("INVALID_INPUT", "Invalid image URL or image ID.", 400)

    # safety check before retrying a completed image to block unneeded retry calls
    if image_id in processing_queue and processing_queue[image_id]["status"] == "completed":
        return api_error("ALREADY_PROCESSED", f"Image {image_id} was already processed.", 409)

    # requeue if pürocess failed
    if image_id in processing_queue and processing_queue[image_id]["status"] == "failed":
        print(f"Retrying failed image {image_id}...")
        processing_queue[image_id]["status"] = "processing"

    # If already queued and still processing, accept silently (idempotent)
    elif image_id in processing_queue and processing_queue[image_id]["status"] != "completed":
        return api_success({
            "message": f"Image {image_id} is already queued for processing."
        })


    image_path = os.path.join(UPLOAD_FOLDER, f"image_{image_id}.jpg")

    try:
        # Download the image
        response = requests.get(image_url, stream=True)
        if response.status_code == 200:
            with open(image_path, 'wb') as f:
                for chunk in response.iter_content(1024):
                    f.write(chunk)
        else:
            return api_error("ML_PROCESSING_FAILED", "Failed to download image.", 502)


    except requests.RequestException as e:
        # return jsonify({"error": str(e)}), 500
        return api_error("500_INTERNAL_ERROR", str(e), 500)

    # Add to processing queue
    processing_queue[image_id] = {
        "image_path": image_path,
        "status": "processing"
    }

    # ✅ Immediately return a success response to Django
    response_message = {
        "message": f"Image {image_id} received, processing started."
    }
    #return jsonify(response_message), 200
    return api_success({
        "message": f"Image {image_id} received, processing started."
    })



def background_process_images():
    """Background task to process images asynchronously and send results to Django."""
    while True:
        for image_id, data in list(processing_queue.items()):
            if data["status"] == "processing":
                print(f"Processing image {image_id}...")

                # Simulate ML processing delay
                time.sleep(30)

                # Run Dummy Apple Detection
                nb_apples, confidence_score = detect_apples(data["image_path"])

                if nb_apples is None:
                    print(f"Error processing image {image_id}.")
                    continue

                # ✅ Send processed data to Django
                ml_result_payload = {
                    "image_id": image_id,
                    "nb_apples": nb_apples,
                    "confidence_score": confidence_score,
                    "processed": True
                }

                try:
                    django_response = requests.post(
                        f"{DJANGO_API_URL}{image_id}/ml_result/",
                        json=ml_result_payload
                    )

                    if django_response.status_code == 200:
                        print(f"✅ ML Results for Image {image_id} successfully sent to Django.")
                        processing_queue[image_id]["status"] = "completed"
                    else:
                        print(f"❌ Error sending ML results for Image {image_id}.")

                except requests.RequestException as e:
                    print(f"❌ Failed to send ML results to Django: {str(e)}")


@app.route('/ml/version', methods=['GET'])
def get_version():
    """Returns the current ML model version and status (wrapped)."""
    
    return api_success({
        "model_version": ML_MODEL_VERSION,
        "status": "active",
        "last_updated": LAST_UPDATED
    })


if __name__ == '__main__':
    import threading

    # Start background processing thread
    processing_thread = threading.Thread(target=background_process_images, daemon=True)
    processing_thread.start()

    app.run(host="0.0.0.0", port=5000, debug=False)

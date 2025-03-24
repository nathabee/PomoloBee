# model/detect.py

import cv2
import numpy as np

def detect_apples_opencv(image_path):
    """
    Simulates apple detection using OpenCV dummy logic.
    Replace this with real detection later (e.g. YOLO).
    """
    img = cv2.imread(image_path)
    if img is None:
        return None, None

    # Dummy logic: simulate apple count and confidence
    nb_apples = np.random.randint(5, 20)
    confidence_score = round(np.random.uniform(0.7, 0.95), 2)
    return nb_apples, confidence_score

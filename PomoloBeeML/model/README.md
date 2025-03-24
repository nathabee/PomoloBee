# 🍎 PomoloBee ML Model Folder

This folder contains the model logic for detecting apples and estimating yield.

## 🧠 Detection Modes

| File | Purpose |
|------|---------|
| `detect.py` | Main entry point for detection (currently dummy using OpenCV) |
| `yolo_utils.py` | Reserved for YOLOv8 integration |
| `best.pt` | (Not yet added) Trained model weights |

## 🔄 Upgrade Plan
In Phase 2+ we will:
- Load YOLOv8 model via ultralytics or ONNX
- Replace `detect_apples_opencv` with real inference pipeline

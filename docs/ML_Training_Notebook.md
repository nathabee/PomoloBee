
# Pomolobee fruit Detection - YOLOv8 Training Notebook

This notebook walks through training a YOLOv8 model to detect fruit using labeled image data.

---

## Step 1 Install YOLOv8 Ultralytics

```bash
!pip install ultralytics
```

---

## ️ Step 2 Dataset Preparation

Make sure your dataset is structured as:

```
data/
├── images/
│   ├── train/
│   └── val/
├── labels/
│   ├── train/
│   └── val/
└── data.yaml
```

- `images/`: Contains `.jpg` or `.png` image files.
- `labels/`: Contains YOLO-format `.txt` files for each image.
- `data.yaml`: Describes dataset path and classes.

---

## Step 3 Check your data.yaml

Example `data.yaml`:

```yaml
train: ./data/images/train
val: ./data/images/val
nc: 1
names: ["fruit"]
```

---

## Step 4 Train YOLOv8 Model

```python
from ultralytics import YOLO

# Load a pre-trained model
model = YOLO('yolov8n.pt')  # yolov8n = nano, fast and light

# Train the model
model.train(data='data/data.yaml', epochs=30, imgsz=640)
```

---

## Step 5 Evaluate the Model

```python
metrics = model.val()
print(metrics)
```

---

## Step 6 Run Inference on a Sample Image

```python
results = model('data/images/val/sample.jpg')
results.show()  # Show image with predictions
results.save()  # Save predictions to file
```

---

## Notes

- You can also use `yolov8s.pt`, `yolov8m.pt`, or `yolov8l.pt` for larger models.
- Tune parameters like `epochs`, `imgsz`, and learning rate for better performance.
- For deployment, save the `best.pt` weights to `PomoloBeeML/model/`.

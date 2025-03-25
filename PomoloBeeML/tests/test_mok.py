#!/bin/bash

echo "🧪 Starting Flask MOK test..."

# === CONFIG ===
BACKUP_FILE="flask_config.json.bak"
TEST_CONFIG_FILE="flask_config.test.json"
LOG_FILE="flask_debug.log"
FLASK_HOST="http://localhost:5000"
IMAGE_ID=999
IMAGE_URL="https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Apple.jpg/640px-Red_Apple.jpg"

# === STEP 1: Backup original config ===
if [ -f flask_config.json ]; then
    cp flask_config.json "$BACKUP_FILE"
    echo "✅ Backup of original config created: $BACKUP_FILE"
fi

# === STEP 2: Write test config ===
cat <<EOF > flask_config.json
{
  "DEBUG": true,
  "MOK": true,
  "MOK_CODE": 200,
  "MOK_RETURN": { "message": "MOK running successfully" },
  "MOK_DELAY": 3,
  "MOK_MLRESULT": {
    "nb_apples": 7,
    "confidence_score": 0.92,
    "processed": true
  },
  "DJANGO_API_URL": "http://localhost:8000/api/images/",
  "LOG_FILE": "$LOG_FILE",
  "ML_MODEL_VERSION": "vX.Y.TEST",
  "LAST_UPDATED": "2099-12-31T23:59:59"
}
EOF
echo "✅ Test config written to flask_config.json"

# === STEP 3: Start Flask in background ===
echo "🚀 Launching Flask app in background..."
python3 app.py & 
FLASK_PID=$!

# Wait for Flask to start
sleep 2

# === STEP 4: Test /ml/version ===
echo "🔍 Testing /ml/version endpoint"
curl -s "$FLASK_HOST/ml/version" | jq

# === STEP 5: Simulate POST /ml/process-image ===
echo "📤 Posting image to trigger mock..."
curl -s -X POST "$FLASK_HOST/ml/process-image" \
    -H "Content-Type: application/json" \
    -d '{
        "image_id": "'"$IMAGE_ID"'",
        "image_url": "'"$IMAGE_URL"'"
    }' | jq

echo "⏳ Waiting for MOK_DELAY (${FLASK_CONFIG["MOK_DELAY"]:-3}) seconds + buffer..."
sleep 5

# === STEP 6: Check log output ===
echo "📄 Showing last 10 lines from log ($LOG_FILE):"
tail -n 10 "$LOG_FILE"

# === STEP 7: Test error case (image does not exist)
echo "🚫 Posting invalid image ID to test error response..."
curl -s -X POST "$FLASK_HOST/ml/process-image" \
    -H "Content-Type: application/json" \
    -d '{
        "image_id": 1234,
        "image_url": "https://example.com/image_not_found.jpg"
    }' | jq

# === STEP 8: Simulate forced error (MOK_CODE 500) ===
echo "🧪 Testing forced MOK error..."
cat <<EOF > flask_config.json
{
  "DEBUG": true,
  "MOK": true,
  "MOK_CODE": 500,
  "MOK_RETURN": { "message": "Simulated MOK error" },
  "MOK_DELAY": 1,
  "MOK_MLRESULT": {
    "nb_apples": 0,
    "confidence_score": 0,
    "processed": false
  },
  "DJANGO_API_URL": "http://localhost:8000/api/images/",
  "LOG_FILE": "$LOG_FILE",
  "ML_MODEL_VERSION": "vERROR.TEST",
  "LAST_UPDATED": "2099-01-01T00:00:00"
}
EOF

sleep 2

curl -s -X POST "$FLASK_HOST/ml/process-image" \
    -H "Content-Type: application/json" \
    -d '{
        "image_id": 777,
        "image_url": "'"$IMAGE_URL"'"
    }' | jq


# === STEP 9: Cleanup ===
echo "🧹 Cleaning up..."

kill $FLASK_PID
wait $FLASK_PID 2>/dev/null

if [ -f "$BACKUP_FILE" ]; then
    mv "$BACKUP_FILE" flask_config.json
    echo "✅ Restored original flask_config.json"
fi

echo "✅ MOK test completed."

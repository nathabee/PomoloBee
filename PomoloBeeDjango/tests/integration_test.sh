#!/bin/bash
set -e  # Stop on error

#sudo apt update
#sudo apt install jq 
GREEN="\033[0;32m"
RED="\033[0;31m"
NC="\033[0m" # No Color
HAS_ERRORS=0

echo -e "${GREEN}Integration test.${NC}"


# 🛑 Check if we're in the correct root folder
if [ ! -f "manage.py" ]; then
  echo -e "${RED}❌ ERROR: Please run this script from the Django project root (where manage.py is located)." >&2
  echo "💡 Tip: cd to the root directory (e.g., PomoloBeeDjango) and run:" >&2
  echo "       ./core/tests/integration_workflow.sh" >&2
  exit 1 
fi

command -v jq >/dev/null 2>&1 || { echo >&2 "❌ jq is required but not installed."; exit 1; }


API_URL="http://127.0.0.1:8000/api"
IMAGE_PATH="./media/images/orchard.jpg"
export IGNORE_VOLATILE=true


# Optionally: check image exists
if [ ! -f "$IMAGE_PATH" ]; then
  echo -e "${RED}❌ ERROR: Image not found at $IMAGE_PATH" >&2
  exit 1
fi

MODE="default"
SNAPSHOT_DIR="./tests/snapshots"
mkdir -p "$SNAPSHOT_DIR"

# Parse mode flag
if [[ "$1" == "--snapshot" ]]; then
  MODE="snapshot"
  echo "📸 Snapshot generation mode"
elif [[ "$1" == "--nonreg" ]]; then
  MODE="nonreg"
  echo "🔍 Non-regression test mode"
  IMAGE_ID=$(cat "$SNAPSHOT_DIR/image_id.txt")
elif [[ "$1" == "--integ" ]]; then
  MODE="integ"
  echo "🧪 Integration test mode"
else
  echo -e "${RED}❌ ERROR: Invalid parameter '$1'" >&2
  echo -e "${RED}Usage: $0 [--snapshot | --nonreg | --integ]" >&2
  exit 1
fi
 

# Function: normalize response (removes volatile keys)
normalize_json() {
if [ "$IGNORE_VOLATILE" = true ]; then
  jq --sort-keys 'walk(
    if type == "object" then
      with_entries(select(.key | IN("image_id", "estimation_id", "timestamp", "processed_at", "upload_date", "image_url") | not))
    else .
    end
  )'
else
  jq --sort-keys 'walk(if type == "object" then . else . end)'
fi

}



# Function: save snapshot
save_snapshot() {
  local name="$1"
  local content="$2"
  echo "$content" | jq . > "$SNAPSHOT_DIR/$name.json"
}

# Function: compare to snapshot
compare_snapshot() {
  local name="$1"
  local content="$2"

  expected="$SNAPSHOT_DIR/$name.json"
  if [ ! -f "$expected" ]; then 
    echo -e "${RED}❌ No snapshot found for $name" >&2
    return 1
  fi


  # Normalize both JSONs and compare
  diff_output=$(diff -u <(echo "$content" | normalize_json) <(cat "$expected" | normalize_json))
  if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Regression detected in $name:${NC}" >&2
    echo -e "${RED}$diff_output" >&2
    echo -e  "${RED}❌❌❌DEBUG❌❌❌ $name:${content}" >&2
    echo -e  "${RED}❌❌❌DEBUG ❌❌❌ expected:${expected}" >&2
    HAS_ERRORS=1
    #return 1
  else
    echo  "${GREEN}✅ $name OK${NC}"
  fi
}







echo "-------------------------------"
echo "🍃 Integration Test: PomoloBee"
echo "-------------------------------"

# 🌱 STEP 0 — Load Static Data
echo "🔹 Step 0: App initializes orchard structure"

echo "📡 GET /fields/"
FIELDS=$(curl -s $API_URL/fields/)

case "$MODE" in
  snapshot)
    save_snapshot "step_00_fields" "$FIELDS"
    ;;
  nonreg)
    compare_snapshot "step_00_fields" "$FIELDS"  || HAS_ERRORS=1
    ;;
  *)
    echo "$FIELDS" | jq .
    ;;
esac

echo "📡 GET /fruits/"
FRUITS=$(curl -s $API_URL/fruits/)

case "$MODE" in
  snapshot)
    save_snapshot "step_00_fruits" "$FRUITS"
    ;;
  nonreg)
    compare_snapshot "step_00_fruits" "$FRUITS"   || HAS_ERRORS=1
    ;;
  *)
    echo "$FRUITS" | jq .
    ;;
esac

echo "📡 GET /locations/"
REQ=$(curl -s $API_URL/locations/)

case "$MODE" in
  snapshot)
    save_snapshot "step_00_locations" "$REQ"
    ;;
  nonreg)
    compare_snapshot "step_00_locations" "$REQ"   || HAS_ERRORS=1
    ;;
  *)
    echo "$REQ" | jq .
    ;;
esac

 


echo "📡 GET /ml/version/"
REQ=$(curl -s $API_URL/ml/version/)

case "$MODE" in
  snapshot)
    save_snapshot "step_00_versions" "$REQ"
    ;;
  nonreg)
    compare_snapshot "step_00_versions" "$REQ"   || HAS_ERRORS=1
    ;;
  *)
    echo "$REQ" | jq .
    ;;
esac 

# 📸 STEP 1 — Upload Image
echo ""
echo "🖼️ Step 1: Uploading image"
UPLOAD_RESPONSE=$(curl -s -F "image=@$IMAGE_PATH" -F "row_id=1" -F "date=2024-03-14" "$API_URL/images/")

# Try to parse JSON
if echo "$UPLOAD_RESPONSE" | jq -e . >/dev/null 2>&1; then
  echo "$UPLOAD_RESPONSE" | jq .

  IMAGE_ID=$(echo "$UPLOAD_RESPONSE" | jq .data.image_id)
  if [[ "$IMAGE_ID" == "null" || -z "$IMAGE_ID" ]]; then
    echo "❌ Failed to get image ID from JSON. Aborting."
    exit 1
  fi
else
  echo -e "${RED}⚠️ Warning: Response is not valid JSON:" >&2
  echo -e "${RED}$UPLOAD_RESPONSE" >&2
  echo -e "${RED}❌ Cannot extract image ID. Aborting." >&2
  exit 1 
fi

echo "✅ Image uploaded with ID: $IMAGE_ID"


# 🧠 STEP 2 — Wait for ML (mock delay or real)
echo ""
echo "⏳ Step 2: Waiting for ML to process (4s delay)"
sleep 4

# You can skip this if ML is already responding for real during the test
# echo ""
# echo "🧠 Step 2: Simulating ML result (for manual test)"
# ML_RESPONSE=$(curl -s -X POST "$API_URL/images/$IMAGE_ID/ml_result" \
#   -H "Content-Type: application/json" \
#   -d '{"nb_fruit": 10, "confidence_score": 0.95}')


# 🔍 STEP 3 — Poll for status
echo ""
echo "🔁 Step 3: Polling image metadata"
IMAGE_META=$(curl -s "$API_URL/images/$IMAGE_ID/details/")

case "$MODE" in
  snapshot)
    save_snapshot "step_03_image_meta" "$IMAGE_META"
    ;;
  nonreg)
    compare_snapshot "step_03_image_meta" "$IMAGE_META"  || HAS_ERRORS=1 
    ;;
  *)
    echo "$IMAGE_META" | jq .
    ;;
esac

PROCESSED=$(echo "$IMAGE_META" | jq .data.processed)
if [ "$PROCESSED" != "true" ]; then
  echo ""
  echo "🔁 Retrying processing (optional fallback)"
  RETRY=$(curl -s -X POST "$API_URL/retry_processing/" \
    -H "Content-Type: application/json" \
    -d "{\"image_id\": $IMAGE_ID}")
  
  echo "$RETRY" | jq .

  if [[ "$MODE" == "snapshot" ]]; then
    save_snapshot "step_06_retry_response" "$RETRY"
  elif [[ "$MODE" == "nonreg" ]]; then
    compare_snapshot "step_06_retry_response" "$RETRY"  || HAS_ERRORS=1
  fi
fi



# 📊 STEP 4 — Fetch Estimation
echo ""
echo "📈 Step 4: Getting Estimation"
ESTIMATION=$(curl -s "$API_URL/images/$IMAGE_ID/estimations/")

case "$MODE" in
  snapshot)
    save_snapshot "step_04_estimation" "$ESTIMATION"
    ;;
  nonreg)
    compare_snapshot "step_04_estimation" "$ESTIMATION"  || HAS_ERRORS=1
    ;;
  *)
    echo "$ESTIMATION" | jq .
    ;;
esac


# 🗂️ STEP 5 — Get Estimation History for Field
echo ""
echo "📚 Step 5: Getting field estimation history"
FIELD_ID=1
FIELD_HISTORY_RAW=$(curl -s "$API_URL/fields/$FIELD_ID/estimations/")

# Normalize field history: only keep the most recent one (first in list)
FIELD_HISTORY=$(echo "$FIELD_HISTORY_RAW" | jq '{status, data: {estimations: [.data.estimations[0]]}}')

case "$MODE" in
  snapshot)
    save_snapshot "step_05_field_history" "$FIELD_HISTORY"
    ;;
  nonreg)
    compare_snapshot "step_05_field_history" "$FIELD_HISTORY" || HAS_ERRORS=1
    ;;
  *)
    echo "$FIELD_HISTORY" | jq .
    ;;
esac


# ♻️ STEP 6 - OPTIONAL: Retry if not processed
if [ "$PROCESSED" != "true" ]; then
  echo ""
  echo "🔁 Retrying processing (optional fallback)"
  curl -s -X POST "$API_URL/retry_processing/" \
    -H "Content-Type: application/json" \
    -d "{\"image_id\": $IMAGE_ID}" | jq .
fi


if [ "$MODE" == "snapshot" ]; then
  echo "$IMAGE_ID" > "$SNAPSHOT_DIR/image_id.txt"
fi

 
# 🗑️ STEP 7 — Delete the Image
echo ""
echo "🗑️ Step 7: Deleting the uploaded image"
DELETE_RESPONSE=$(curl -s -X DELETE "$API_URL/images/$IMAGE_ID/")


if ! echo "$DELETE_RESPONSE" | jq -e . >/dev/null 2>&1; then
  echo "$DELETE_RESPONSE" > /tmp/delete_debug.html
  echo -e "${RED}⚠️ Response is not valid JSON. Saved to /tmp/delete_debug.html${NC}"
fi



case "$MODE" in
  snapshot)
    save_snapshot "step_07_delete_response" "$DELETE_RESPONSE"
    ;;
  nonreg)
    compare_snapshot "step_07_delete_response" "$DELETE_RESPONSE" || HAS_ERRORS=1
    ;;
  *)
    if echo "$DELETE_RESPONSE" | jq -e . >/dev/null 2>&1; then
      echo "$DELETE_RESPONSE" | jq .
    else
      echo "$DELETE_RESPONSE"
    fi
    ;;
esac


# 🔍 Verify image deletion
echo ""
echo "🔍 Verifying deletion (should error or show missing):"
DELETE_VERIFY=$(curl -s "$API_URL/images/$IMAGE_ID/details/")

if echo "$DELETE_VERIFY" | jq -e . >/dev/null 2>&1; then
  echo "$DELETE_VERIFY" | jq .
else
  echo "$DELETE_VERIFY"
fi

##########################
# 🧠 STEP 8 — Simulate ML Result on Non-Existing Image
echo ""
echo "🧠 Step 8: Simulating ML result on a non-existing image (should fail)"
HTTP_STATUS=$(curl -s -o /tmp/ml_invalid_resp.json -w "%{http_code}" -X POST "$API_URL/images/999999/ml_result/" \
  -H "Content-Type: application/json" \
  -d '{"nb_fruit": 10, "confidence_score": 0.95, "processed": true}')

INVALID_ML_RESPONSE=$(cat /tmp/ml_invalid_resp.json)

# Combine response + status code into one JSON object
COMBINED_ML_RESULT=$(jq -n \
  --arg status "$HTTP_STATUS" \
  --argjson response "$(echo "$INVALID_ML_RESPONSE" | jq . 2>/dev/null)" \
  '{http_status: $status|tonumber, response: $response}' 2>/dev/null || \
  echo "{\"http_status\": $HTTP_STATUS, \"response\": \"$INVALID_ML_RESPONSE\"}")

case "$MODE" in
  snapshot)
    echo "$COMBINED_ML_RESULT" | jq . > "$SNAPSHOT_DIR/step_08_invalid_ml_result.json"
    ;;
  nonreg)
    compare_snapshot "step_08_invalid_ml_result" "$COMBINED_ML_RESULT" || HAS_ERRORS=1
    ;;
  *)
    if echo "$COMBINED_ML_RESULT" | jq -e . >/dev/null 2>&1; then
      echo "$COMBINED_ML_RESULT" | jq .
    else
      echo "$COMBINED_ML_RESULT"
    fi
    ;;
esac



#############################


 

echo ""
echo -e "ℹ️  To update snapshots after legitimate changes, run:"
echo -e "   ${GREEN}$0 --snapshot${NC}"
echo ""


if [ "$MODE" == "nonreg" ]; then
  if [ "$HAS_ERRORS" -eq 1 ]; then
    echo -e "${RED}❌ Non-regression test failed due to detected differences.${NC}" >&2
    exit 1
  else
    echo -e "${GREEN}✅ No regression detected. All API responses are consistent.${NC}"
  fi
else 
  echo -e "$✅ Full integration test completed."
  echo -e "Run the script to check that there is no regression."
  echo -e "$0 --nonreg"
fi


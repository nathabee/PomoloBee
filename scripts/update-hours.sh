#!/bin/bash

WORKLOG_FILE="documentation/WORKLOG.md"
README_FILE="README.md"

# Check if the WORKLOG.md file exists, if not, exit with an error
if [ ! -f "$WORKLOG_FILE" ]; then
  echo "Error: WORKLOG.md file not found!"
  exit 1
fi

# Extract total hours worked from WORKLOG.md
total_hours=0

# Use grep to find "Hours Worked" occurrences, but exclude the auto-generated line
total_hours=$(grep -oP '(?<=- \*\*Hours Worked\*\*: )\d+' "$WORKLOG_FILE" | awk '{s+=$1} END {print s}')

echo "Total hours calculated: ${total_hours}"

# Python script to update the markdown files
update_files() {
    local file=$1
    local total_hours=$2

    python3 << EOF
import re

file_path = "$file"
total_hours = "$total_hours"

with open(file_path, 'r') as file:
    content = file.read()

# Update only the auto-generated "Total Hours Worked" line, not individual logs
content = re.sub(
    r"(!\[⏱️\]\(.*?\) \*\*Total Hours Worked\*\*: _)\d+ hours(_ \(Auto-generated\))",
    r"\g<1>{} hours\g<2>".format(total_hours),
    content
)

with open(file_path, 'w') as file:
    file.write(content)

print(f"Updated {file_path} successfully.")
EOF
}

# Ensure both WORKLOG.md and README.md are updated
update_files "$WORKLOG_FILE" "$total_hours"
if [ -f "$README_FILE" ]; then
    update_files "$README_FILE" "$total_hours"
else
    echo "Warning: README.md file not found!"
fi

echo "Script finished. Please verify changes."

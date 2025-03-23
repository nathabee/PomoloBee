import sys
import re

# --- Regex to match emojis, including 1️⃣-style keycaps ---
emoji_pattern = re.compile(
    "[" 
    "\U0001F600-\U0001F64F"  # emoticons
    "\U0001F300-\U0001F5FF"  # symbols & pictographs
    "\U0001F680-\U0001F6FF"  # transport
    "\U0001F1E0-\U0001F1FF"  # flags
    "\U00002500-\U00002BEF"  # Chinese/Japanese
    "\U00002700-\U000027BF"  # Dingbats
    "\U0001F900-\U0001F9FF"  # supplemental symbols
    "\U0001FA70-\U0001FAFF"  # extended pictographs
    "\u200d"                 # Zero Width Joiner
    "\u2640-\u2642"
    "\u2600-\u26FF"
    "\u23E9-\u23FA"
    "]+", flags=re.UNICODE)

# --- Clean bold formatting ---
def clean_bold_spaces(text):
    return re.sub(r'\*\*\s*(.*?)\s*\*\*', r'**\1**', text)

def clean_markdown_headers(file_path):
    header_regex = re.compile(r'^(#{1,6})(\s+)(.*)')

    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    new_lines = []
    for line in lines:
        match = header_regex.match(line)
        if match:
            hashes, space, content = match.groups()
            content = emoji_pattern.sub('', content)
            content = clean_bold_spaces(content)
            new_lines.append(f"{hashes}{space}{content.strip()}\n")
        else:
            new_lines.append(line)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python remove_emojis.py <markdown_file>")
        sys.exit(1)

    clean_markdown_headers(sys.argv[1])

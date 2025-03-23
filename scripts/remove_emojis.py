import sys
import re

keycap_map = {
    '0️⃣': '0', '1️⃣': '1', '2️⃣': '2', '3️⃣': '3', '4️⃣': '4',
    '5️⃣': '5', '6️⃣': '6', '7️⃣': '7', '8️⃣': '8', '9️⃣': '9',
    '#️⃣': '#', '*️⃣': '*',
}

emoji_pattern = re.compile(
    "[" 
    "\U0001F600-\U0001F64F"
    "\U0001F300-\U0001F5FF"
    "\U0001F680-\U0001F6FF"
    "\U0001F1E0-\U0001F1FF"
    "\U00002700-\U000027BF"
    "\U0001F900-\U0001F9FF"
    "\U0001FA70-\U0001FAFF"
    "\u200d"
    "\u2640-\u2642"
    "\u2600-\u26FF"
    "\u23E9-\u23FA"
    "]+", flags=re.UNICODE)

arrow_map = {
    '→': ' to ',
    '←': ' from ',
    '↔': ' both ',
    '➔': ' to ',
    '➡': ' to ',
}

# Characters to remove from headers
SPECIAL_CHARS = r"[()&,:–—]"

def replace_keycap_emojis(text):
    for emoji, digit in keycap_map.items():
        text = text.replace(emoji, digit)
    return text

def clean_bold_spaces(text):
    return re.sub(r'\*\*\s*(.*?)\s*\*\*', r'**\1**', text)

def strip_special_chars(text):
    return re.sub(SPECIAL_CHARS, '', text)

def clean_markdown_headers(file_path):
    header_regex = re.compile(r'^(#{1,6})(\s+)(.*)')

    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    new_lines = []
    for line in lines:
        match = header_regex.match(line)
        if match:
            hashes, space, content = match.groups()
            content = replace_keycap_emojis(content)
            content = emoji_pattern.sub('', content)
            content = clean_bold_spaces(content) 
            for arrow, word in arrow_map.items():
                content = content.replace(arrow, word)
            content = strip_special_chars(content)
            content = re.sub(r'\s{2,}', ' ', content)  # Collapse multiple spaces into one
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

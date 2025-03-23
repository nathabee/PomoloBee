import sys
import re

def clean_header_emojis(filepath):
    header_pattern = re.compile(r'^(#{1,6})\s+([\W\d_]*)(.*)')
    emoji_pattern = re.compile("[\U0001F300-\U0001FAD6]+")

    with open(filepath, 'r', encoding='utf-8') as file:
        lines = file.readlines()

    new_lines = []
    for line in lines:
        match = header_pattern.match(line)
        if match:
            hashes, prefix, rest = match.groups()
            clean_prefix = emoji_pattern.sub('', prefix).strip()
            clean_line = f"{hashes} {clean_prefix} {rest}".strip() + '\n'
            new_lines.append(clean_line)
        else:
            new_lines.append(line)

    with open(filepath, 'w', encoding='utf-8') as file:
        file.writelines(new_lines)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python remove_emojis.py <markdown_file>")
        sys.exit(1)

    clean_header_emojis(sys.argv[1])

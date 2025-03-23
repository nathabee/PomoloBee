import sys
import re

def clean_header_emojis(filepath):
    # Regex to detect Markdown headers
    header_regex = re.compile(r'^(#{1,6})(\s+)(.*)')

    # Unicode emoji pattern (basic range)
    emoji_pattern = re.compile(
        "[" 
        "\U0001F600-\U0001F64F"  # emoticons
        "\U0001F300-\U0001F5FF"  # symbols & pictographs
        "\U0001F680-\U0001F6FF"  # transport & map
        "\U0001F1E0-\U0001F1FF"  # flags
        "\U00002700-\U000027BF"  # dingbats
        "\U0001F900-\U0001F9FF"  # supplemental symbols
        "\U0001FA70-\U0001FAFF"  # extended pictographs
        "]+", flags=re.UNICODE)

    with open(filepath, 'r', encoding='utf-8') as file:
        lines = file.readlines()

    new_lines = []
    for line in lines:
        match = header_regex.match(line)
        if match:
            hashes, space, header_text = match.groups()
            clean_text = emoji_pattern.sub('', header_text).strip()
            new_lines.append(f"{hashes}{space}{clean_text}\n")
        else:
            new_lines.append(line)

    with open(filepath, 'w', encoding='utf-8') as file:
        file.writelines(new_lines)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python remove_emojis.py <markdown_file>")
        sys.exit(1)

    clean_header_emojis(sys.argv[1])

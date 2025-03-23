import sys
import re

def clean_header_emojis_and_spaces(filepath):
    header_regex = re.compile(r'^(#{1,6})(\s+)(.*)')
    emoji_pattern = re.compile(
        "[" 
        "\U0001F600-\U0001F64F"
        "\U0001F300-\U0001F5FF"
        "\U0001F680-\U0001F6FF"
        "\U0001F1E0-\U0001F1FF"
        "\U00002700-\U000027BF"
        "\U0001F900-\U0001F9FF"
        "\U0001FA70-\U0001FAFF"
        "]+", flags=re.UNICODE)

    def clean_bold_spaces(text):
        return re.sub(r'\*\*\s*(.*?)\s*\*\*', r'**\1**', text)

    with open(filepath, 'r', encoding='utf-8') as file:
        lines = file.readlines()

    new_lines = []
    for line in lines:
        match = header_regex.match(line)
        if match:
            hashes, space, header_text = match.groups()
            no_emoji = emoji_pattern.sub('', header_text)
            clean_text = clean_bold_spaces(no_emoji.strip())
            new_lines.append(f"{hashes}{space}{clean_text}\n")
        else:
            new_lines.append(line)

    with open(filepath, 'w', encoding='utf-8') as file:
        file.writelines(new_lines)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python remove_emojis.py <markdown_file>")
        sys.exit(1)

    clean_header_emojis_and_spaces(sys.argv[1])

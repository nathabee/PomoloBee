import sys
import re 
import unicodedata

def github_anchor(title):
    # Remove Markdown formatting
    title = re.sub(r'\*\*(.*?)\*\*', r'\1', title)
    title = re.sub(r'[_`~]', '', title)

    # Normalize to NFKD (compatibility form), e.g. remove accents
    title = unicodedata.normalize('NFKD', title)

    # Convert to ASCII-only (strip emojis, special dashes, etc.)
    title = ''.join(c for c in title if not unicodedata.category(c).startswith('So'))  # Remove symbols like emojis
    title = ''.join(c for c in title if c.isalnum() or c in [' ', '-'])

    # Lowercase and convert spaces to hyphens
    title = title.strip().lower()
    title = re.sub(r'\s+', '-', title)
    title = re.sub(r'-+', '-', title)

    return title


def generate_toc(filename, depth):
    toc_lines = []
    with open(filename, 'r') as file:
        lines = file.readlines()
        in_code_block = False

        for line in lines:
            # Ignore code blocks
            if line.strip().startswith("```"):
                in_code_block = not in_code_block
                continue

            if not in_code_block:
                match = re.match(r'^(#{1,' + str(depth) + r'})\s+(.*)', line)
                if match:
                    level = len(match.group(1))
                    title = match.group(2)
                    link = github_anchor(title)
                    toc_lines.append(f"{'  ' * (level - 1)}- [{title}](#{link})")

    return '\n'.join(toc_lines)

def update_toc(filename, depth):
    with open(filename, 'r') as file:
        content = file.read()

    toc_content = generate_toc(filename, depth)
    new_content = re.sub(r'<!-- TOC -->.*<!-- TOC END -->',
                         f'<!-- TOC -->\n{toc_content}\n<!-- TOC END -->',
                         content, flags=re.DOTALL)

    with open(filename, 'w') as file:
        file.write(new_content)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python create_tabcont.py <filename> <depth>")
        sys.exit(1)

    filename = sys.argv[1]
    depth = int(sys.argv[2])
    update_toc(filename, depth)

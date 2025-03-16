import sys
import re

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
                    link = re.sub(r'[^a-zA-Z0-9 ]', '', title).strip().replace(' ', '-').lower()
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

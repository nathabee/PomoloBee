import re
import sys

def generate_toc(filename, levels=1):
    if levels not in [1, 2, 3]:
        print("Please specify a valid level: 1, 2, or 3.")
        return
    
    # Regex patterns for each header level
    patterns = [
        (1, r'^# (.*)'),   # Level 1: `# Header`
        (2, r'^## (.*)'),  # Level 2: `## Header`
        (3, r'^### (.*)')  # Level 3: `### Header`
    ]

    toc = []
    try:
        with open(filename, 'r', encoding='utf-8') as file:
            for line in file:
                for level, pattern in patterns:
                    if level <= levels:
                        match = re.match(pattern, line.strip())
                        if match:
                            header_text = match.group(1).strip()
                            # Convert header text to anchor-friendly format
                            anchor = header_text.lower().replace(' ', '-').replace('[', '').replace(']', '')
                            # Create indentation for sub-levels
                            indent = '  ' * (level - 1)
                            toc.append(f"{indent}- [{header_text}](#{anchor})")
                            break  # Stop checking lower-level patterns for this line
        
        # Output the TOC in Markdown format
        if toc:
            print("## Table of Contents\n")
            for item in toc:
                print(item)
        else:
            print("No headers found at the specified levels.")
    except FileNotFoundError:
        print(f"Error: File '{filename}' not found.")
    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python generate_toc.py <filename> <level>")
    else:
        filename = sys.argv[1]
        try:
            level = int(sys.argv[2])
            generate_toc(filename, level)
        except ValueError:
            print("Level must be an integer (1, 2, or 3).")

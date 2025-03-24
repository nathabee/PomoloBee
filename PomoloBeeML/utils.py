# utils.py

import os
import requests

def download_image(image_url, image_path):
    """
    Download an image from a URL and save it to local path.
    """
    try:
        response = requests.get(image_url, stream=True, timeout=5)
        if response.status_code == 200:
            with open(image_path, 'wb') as f:
                for chunk in response.iter_content(1024):
                    f.write(chunk)
            return True
        return False
    except requests.RequestException:
        return False

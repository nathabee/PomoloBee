import pytest
import requests
from app import app  # Import the Flask app
import os

ML_API_URL = os.getenv("DJANGO_API_URL", "http://127.0.0.1:8000/api")

@pytest.fixture
def client():
    """Creates a test client for Flask."""
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client

def test_ml_receives_image(client):
    """Test if ML service correctly receives an image from Django."""
    payload = {"image_url": "https://nathabee.de/wordpress/wp-content/themes/twentytwentyfive/assets/images/1886c466-b24a-4b0e-8512-cfce37e16419.jpeg", "image_id": 24}
    response = client.post("/process", json=payload)

    assert response.status_code == 200
    assert "message" in response.json

def test_ml_sends_results_to_django():
    """Test if ML correctly sends processed results back to Django."""
    payload = {"nb_apples": 20, "confidence_score": 0.9, "processed": True}
    response = requests.post(f"{ML_API_URL}/images/24/ml_result/", json=payload)

    assert response.status_code == 200
    assert "message" in response.json

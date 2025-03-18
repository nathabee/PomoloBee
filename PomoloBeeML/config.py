import os

class Config:
    UPLOAD_FOLDER = os.path.join(os.getcwd(), "uploads")
    DEBUG = True

app.config.from_object(Config)

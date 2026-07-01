"""Chatbot configuration constants."""

# Spring Boot Backend URL
BASE_URL = "http://localhost:8080/api/dashboard"

# CORS - Only allow requests from Spring Boot frontend
CORS_ORIGINS = [
    "http://localhost:8080",
    "http://127.0.0.1:8080",
]

# Cache settings
CACHE_TTL = 300  # seconds (5 minutes)

# API client settings
API_TIMEOUT = 10.0  # seconds

# Input validation
MAX_QUESTION_LENGTH = 500

# Logging
LOG_LEVEL = "INFO"
LOG_FORMAT = "%(asctime)s [%(levelname)s] %(name)s: %(message)s"

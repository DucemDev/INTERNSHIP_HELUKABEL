"""Gemini LLM client wrapper for Helukabel chatbot.

This module provides a simple interface to Google Gemini models using the
official ``google-generativeai`` SDK. It abstracts API key handling, model
selection and basic error handling.

Features:
- Automatic retry with exponential backoff on 503/429 errors
- Fallback model chain when primary model is overloaded
- Configurable via environment variables

Usage example::

    from chatbot.gemini_client import GeminiClient
    client = GeminiClient()
    response = client.generate_content(
        prompt="Explain the current lead status.",
        system_instruction="You are an assistant for Helukabel CRM. Answer in Vietnamese.")
    print(response)
"""

import os
import time
import logging
from typing import Optional

# Load environment variables from a .env file if present
try:
    from dotenv import load_dotenv
    load_dotenv()
except Exception:
    # dotenv optional; ignore if not installed
    pass

# Lazy import of the official SDK.
try:
    from google import genai  # type: ignore
    from google.genai import types  # type: ignore
    from google.genai import errors as genai_errors  # type: ignore
except Exception as e:  # pragma: no cover
    genai = None
    types = None
    genai_errors = None
    logging.getLogger(__name__).warning("google-genai SDK not available: %s", e)

logger = logging.getLogger(__name__)

# Fallback model chain – tried in order when the primary model is unavailable
FALLBACK_MODELS = [
    "gemini-2.5-flash",
    "gemini-3.5-flash",
]

# Retry settings
MAX_RETRIES = 3
INITIAL_BACKOFF = 2  # seconds


class GeminiClient:
    """Simple wrapper around Google Gemini models.

    The client reads the API key from the ``GEMINI_API_KEY`` environment
    variable. If the variable is missing an ``EnvironmentError`` is raised –
    this makes configuration problems obvious during development.
    """

    def __init__(self, model_name: Optional[str] = None) -> None:
        if genai is None or types is None:
            raise ImportError("google-genai SDK is required for Gemini integration.")
        api_key = os.getenv("GEMINI_API_KEY")
        if not api_key:
            raise EnvironmentError("GEMINI_API_KEY environment variable not set.")
        # Initialize a client with the API key
        self.client = genai.Client(api_key=api_key)
        self.model_name = model_name or os.getenv("GEMINI_MODEL", "gemini-2.5-flash")
        logger.info("GeminiClient initialised with model %s", self.model_name)

    def _call_model(
        self,
        model_name: str,
        prompt: str,
        config: "types.GenerateContentConfig",
    ) -> str:
        """Make a single generate_content call to a specific model."""
        response = self.client.models.generate_content(
            model=model_name,
            contents=prompt,
            config=config,
        )
        return (response.text or "").strip()

    def _is_retryable(self, exc: Exception) -> bool:
        """Check if an exception is a transient error worth retrying."""
        error_str = str(exc).lower()
        # 503 Service Unavailable or 429 Too Many Requests
        if "503" in error_str or "429" in error_str:
            return True
        if "unavailable" in error_str or "overloaded" in error_str:
            return True
        if "resource exhausted" in error_str or "rate limit" in error_str:
            return True
        return False

    def generate_content(
        self,
        prompt: str,
        system_instruction: Optional[str] = None,
        temperature: float = 0.7,
        max_output_tokens: Optional[int] = None,
    ) -> str:
        """Generate a response from the Gemini model.

        Includes automatic retry with exponential backoff and fallback
        to alternative models when the primary model returns 503/429.
        """
        config = types.GenerateContentConfig(
            temperature=temperature,
            max_output_tokens=max_output_tokens,
            system_instruction=system_instruction,
        )

        # Build the list of models to try: primary first, then fallbacks
        models_to_try = [self.model_name]
        for fb in FALLBACK_MODELS:
            if fb != self.model_name:
                models_to_try.append(fb)

        last_error = None

        for model_name in models_to_try:
            # Retry each model up to MAX_RETRIES times
            for attempt in range(1, MAX_RETRIES + 1):
                try:
                    logger.info(
                        "Calling Gemini model=%s (attempt %d/%d), prompt_length=%d chars, max_tokens=%s",
                        model_name, attempt, MAX_RETRIES, len(prompt), max_output_tokens
                    )
                    answer = self._call_model(model_name, prompt, config)

                    if not answer:
                        logger.warning("Gemini returned empty response on model=%s", model_name)
                        break  # Try next model

                    logger.info("Gemini response OK, model=%s, length=%d chars", model_name, len(answer))
                    return answer

                except Exception as exc:
                    last_error = exc
                    is_rate_limit = "429" in str(exc) or "exhausted" in str(exc).lower()
                    
                    if self._is_retryable(exc) and attempt < MAX_RETRIES:
                        # For rate limits (429), wait at least 15s to let the window reset. Otherwise use exponential backoff.
                        wait_time = 15 if is_rate_limit else (INITIAL_BACKOFF * (2 ** (attempt - 1)))
                        logger.warning(
                            "Gemini model=%s returned retryable error (attempt %d/%d). "
                            "Retrying in %ds... Error: %s",
                            model_name, attempt, MAX_RETRIES, wait_time, exc
                        )
                        time.sleep(wait_time)
                        continue
                    elif self._is_retryable(exc):
                        # Exhausted retries for this model, try fallback.
                        # Wait 10s before switching models to prevent cascading 429s.
                        if is_rate_limit:
                            logger.warning("Rate limit hit. Waiting 10s before switching to fallback model...")
                            time.sleep(10)
                        logger.warning(
                            "Gemini model=%s exhausted %d retries. Trying fallback model... Error: %s",
                            model_name, MAX_RETRIES, exc
                        )
                        break  # Break inner retry loop, try next model
                    else:
                        # Non-retryable error, raise immediately
                        logger.error("Gemini non-retryable error on model=%s: %s", model_name, exc, exc_info=True)
                        raise

        # All models and retries exhausted
        logger.error("All Gemini models failed. Last error: %s", last_error)
        if last_error:
            raise last_error
        return "Xin lỗi, tôi không thể tạo nội dung trả lời lúc này. Vui lòng thử lại sau."

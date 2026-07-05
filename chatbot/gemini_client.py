"""Gemini LLM client wrapper for Helukabel chatbot.

This module provides a simple interface to Google Gemini models using the
official ``google-generativeai`` SDK. It abstracts API key handling, model
selection and basic error handling.

Usage example::

    from chatbot.gemini_client import GeminiClient
    client = GeminiClient()
    response = client.generate_content(
        prompt="Explain the current lead status.",
        system_instruction="You are an assistant for Helukabel CRM. Answer in Vietnamese.")
    print(response)
"""

import os
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
except Exception as e:  # pragma: no cover
    genai = None
    types = None
    logging.getLogger(__name__).warning("google-genai SDK not available: %s", e)

logger = logging.getLogger(__name__)


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
        self.model_name = model_name or os.getenv("GEMINI_MODEL", "gemini-3.5-flash")
        logger.info("GeminiClient initialised with model %s", self.model_name)

    def generate_content(
        self,
        prompt: str,
        system_instruction: Optional[str] = None,
        temperature: float = 0.7,
        max_output_tokens: Optional[int] = None,
    ) -> str:
        """Generate a response from the Gemini model."""
        try:
            config = types.GenerateContentConfig(
                temperature=temperature,
                max_output_tokens=max_output_tokens,
                system_instruction=system_instruction,
            )
            response = self.client.models.generate_content(
                model=self.model_name,
                contents=prompt,
                config=config,
            )
            answer = (response.text or "").strip()
            if not answer:
                logger.warning("Gemini returned empty response, using fallback message")
                return "Xin lỗi, tôi không thể tạo nội dung trả lời lúc này. Vui lòng thử lại sau."
            logger.debug("Gemini response: %s", answer[:200])
            return answer
        except Exception as exc:  # pragma: no cover
            logger.error("Gemini generation error: %s", exc)
            raise

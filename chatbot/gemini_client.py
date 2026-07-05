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

# Lazy import of the official SDK.
try:
    import google.generativeai as genai  # type: ignore
except Exception as e:  # pragma: no cover
    genai = None
    logging.getLogger(__name__).warning("google-generativeai SDK not available: %s", e)

logger = logging.getLogger(__name__)


class GeminiClient:
    """Simple wrapper around Google Gemini models.

    The client reads the API key from the ``GEMINI_API_KEY`` environment
    variable. If the variable is missing an ``EnvironmentError`` is raised –
    this makes configuration problems obvious during development.
    """

    def __init__(self, model_name: Optional[str] = None) -> None:
        if genai is None:
            raise ImportError("google-generativeai SDK is required for Gemini integration.")
        api_key = os.getenv("GEMINI_API_KEY")
        if not api_key:
            raise EnvironmentError("GEMINI_API_KEY environment variable not set.")
        genai.configure(api_key=api_key)
        self.model_name = model_name or os.getenv("GEMINI_MODEL", "gemini-1.5-flash")
        self.model = genai.GenerativeModel(self.model_name)
        logger.info("GeminiClient initialised with model %s", self.model_name)

    def generate_content(
        self,
        prompt: str,
        system_instruction: Optional[str] = None,
        temperature: float = 0.7,
        max_output_tokens: Optional[int] = None,
    ) -> str:
        """Generate a response from the Gemini model.
        """
        try:
            response = self.model.generate_content(
                prompt,
                system_instruction=system_instruction,
                temperature=temperature,
                max_output_tokens=max_output_tokens,
            )
            answer = response.text.strip()
            logger.debug("Gemini response: %s", answer[:200])
            return answer
        except Exception as exc:  # pragma: no cover
            logger.error("Gemini generation error: %s", exc)
            raise

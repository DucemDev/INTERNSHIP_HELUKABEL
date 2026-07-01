"""
NLP utilities for Vietnamese text processing.
Key improvement: word boundary matching to prevent false positives.
"""

import unicodedata
import re
import logging

logger = logging.getLogger(__name__)


def remove_accents(text: str) -> str:
    """Remove Vietnamese diacritical marks from text.
    
    Example: 'doanh thu' stays as 'doanh thu'
             'đã kết nối' becomes 'da ket noi'
    """
    text = unicodedata.normalize("NFD", text)
    text = text.encode("ascii", "ignore").decode("utf-8")
    return text


def normalize_question(question: str) -> tuple[str, str]:
    """Normalize a question for intent matching.
    
    Returns:
        (lowercase_original, lowercase_no_accent)
    """
    q = question.lower().strip()
    q_no_accent = remove_accents(q)
    return q, q_no_accent


def has_any_word(text: str, keywords: list[str]) -> bool:
    """Check if any keyword appears in text with word boundary awareness.
    
    For multi-word phrases (contains space): uses substring matching.
    For single words: uses word boundary matching to prevent false positives.
    
    This fixes the critical bug where 'new' would match inside 'renewable',
    or 'won' would match inside 'wonder'.
    """
    for kw in keywords:
        if " " in kw:
            # Multi-word phrase: substring match is safe
            if kw in text:
                return True
        else:
            # Single word: use word boundary to prevent false positives
            pattern = r'(?:^|(?<=\s))' + re.escape(kw) + r'(?=\s|$)'
            if re.search(pattern, text):
                return True
    return False


def keyword_score(text: str, keywords: list[str]) -> int:
    """Calculate how many keywords match in text.
    
    Multi-word phrases get bonus weight (word count).
    Returns total score.
    """
    score = 0
    for kw in keywords:
        if " " in kw:
            if kw in text:
                score += len(kw.split())  # Multi-word gets more weight
        else:
            pattern = r'(?:^|(?<=\s))' + re.escape(kw) + r'(?=\s|$)'
            if re.search(pattern, text):
                score += 1
    return score

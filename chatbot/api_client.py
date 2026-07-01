"""
Async HTTP client with TTL caching for Spring Boot Dashboard API.

Key improvements over original:
- Async (httpx) instead of sync (requests) - prevents blocking under load
- TTL cache - avoids repeated API calls for the same data
- Proper error handling and logging
- Connection reuse via httpx.AsyncClient
"""

import httpx
import logging
import time
from typing import Any

logger = logging.getLogger(__name__)


class CachedAPIClient:
    """Async HTTP client with time-based caching for Dashboard API calls."""

    def __init__(self, base_url: str, cache_ttl: int = 300, timeout: float = 10.0):
        """
        Args:
            base_url: Base URL of the Spring Boot Dashboard API
            cache_ttl: Cache time-to-live in seconds (default: 5 minutes)
            timeout: HTTP request timeout in seconds
        """
        self.base_url = base_url
        self.cache_ttl = cache_ttl
        self.timeout = timeout
        self._cache: dict[str, tuple[float, Any]] = {}
        self._client: httpx.AsyncClient | None = None

    async def _get_client(self) -> httpx.AsyncClient:
        """Get or create the httpx async client."""
        if self._client is None or self._client.is_closed:
            self._client = httpx.AsyncClient(timeout=self.timeout)
        return self._client

    async def close(self):
        """Close the HTTP client and release connections."""
        if self._client and not self._client.is_closed:
            await self._client.aclose()
            logger.info("API client closed.")

    def _get_from_cache(self, endpoint: str) -> Any | None:
        """Get data from cache if not expired."""
        if endpoint in self._cache:
            timestamp, data = self._cache[endpoint]
            if time.time() - timestamp < self.cache_ttl:
                logger.debug(f"Cache HIT: {endpoint}")
                return data
            else:
                del self._cache[endpoint]
                logger.debug(f"Cache EXPIRED: {endpoint}")
        return None

    def _set_cache(self, endpoint: str, data: Any):
        """Store data in cache with current timestamp."""
        self._cache[endpoint] = (time.time(), data)

    def clear_cache(self):
        """Clear all cached data."""
        self._cache.clear()
        logger.info("Cache cleared.")

    async def get(self, endpoint: str) -> Any | None:
        """Make a GET request to the Dashboard API with caching.
        
        Args:
            endpoint: API endpoint path (e.g., '/lead-status')
            
        Returns:
            Parsed JSON response, or None if request failed.
        """
        # Check cache first
        cached = self._get_from_cache(endpoint)
        if cached is not None:
            return cached

        url = self.base_url + endpoint

        try:
            client = await self._get_client()
            response = await client.get(url)

            if response.status_code != 200:
                logger.warning(f"API returned {response.status_code}: {url}")
                return None

            content_type = response.headers.get("content-type", "")
            if "application/json" not in content_type:
                logger.warning(f"Non-JSON response from {url}: {content_type}")
                return None

            data = response.json()
            self._set_cache(endpoint, data)
            logger.debug(f"API OK: {endpoint}")
            return data

        except httpx.TimeoutException:
            logger.error(f"Timeout calling {url}")
            return None
        except httpx.ConnectError:
            logger.error(f"Cannot connect to {url} - is Spring Boot running?")
            return None
        except Exception as e:
            logger.error(f"Error calling {url}: {e}")
            return None

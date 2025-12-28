/**
 * API Configuration
 * Base URLs for API and WebSocket connections
 * 
 * In Docker: Uses relative paths (nginx proxy handles routing)
 * In development: Uses environment variables or defaults to localhost
 */

const getApiBaseUrl = (): string => {
  // If VITE_API_BASE_URL is set, use it (for development)
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL;
  }
  // In production/Docker, use relative path (nginx will proxy to backend)
  // In development, default to localhost
  return import.meta.env.PROD ? '/api' : 'http://localhost:8080/api';
};

const getWebSocketUrl = (): string => {
  // If VITE_WEBSOCKET_URL is set, use it (for development)
  if (import.meta.env.VITE_WEBSOCKET_URL) {
    return import.meta.env.VITE_WEBSOCKET_URL;
  }
  // In production/Docker, use relative path (nginx will proxy to backend)
  // In development, default to localhost
  // SockJS uses http:// or https:// (not ws://), it handles WebSocket upgrade internally
  if (import.meta.env.PROD) {
    const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:';
    return `${protocol}//${window.location.host}/ws`;
  }
  return 'http://localhost:8080/ws';
};

export const apiConfig = {
  baseUrl: getApiBaseUrl(),
  webSocketUrl: getWebSocketUrl(),
};


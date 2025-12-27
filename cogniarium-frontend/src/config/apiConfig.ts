/**
 * API Configuration
 * Base URLs for API and WebSocket connections
 */

const getApiBaseUrl = (): string => {
  return import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
};

const getWebSocketUrl = (): string => {
  return import.meta.env.VITE_WEBSOCKET_URL || 'http://localhost:8080/ws';
};

export const apiConfig = {
  baseUrl: getApiBaseUrl(),
  webSocketUrl: getWebSocketUrl(),
};


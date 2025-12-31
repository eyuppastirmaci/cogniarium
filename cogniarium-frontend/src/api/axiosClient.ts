import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { apiConfig } from '@/config/apiConfig';
import { authService } from '@/features/auth/api/authService';

const apiClient = axios.create({
  baseURL: apiConfig.baseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds timeout
});

// Request interceptor: Add access token to all requests
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = authService.getAccessToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor: Handle 401 errors and refresh token
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (error?: any) => void;
}> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Handle network errors
    if (!error.response) {
      if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        error.message = 'Request timeout. Please check your connection and try again.';
      } else if (error.message.includes('Network Error')) {
        error.message = 'Network error. Please check your internet connection.';
      }
      return Promise.reject(error);
    }

    // Handle 401 Unauthorized - Try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Don't retry refresh for login/register endpoints
      if (originalRequest.url?.includes('/auth/login') || 
          originalRequest.url?.includes('/auth/register')) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // If already refreshing, queue this request
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers && token) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return apiClient(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const newAccessToken = await authService.refreshToken();
        processQueue(null, newAccessToken);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        }
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        // Clear tokens and redirect to login
        authService.clearTokens();
        authService.clearUser();
        // Redirect will be handled by router guard
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // Handle 403 Forbidden - Email not verified
    if (error.response?.status === 403) {
      const message = (error.response.data as any)?.message || 'Access denied';
      if (message.includes('email') || message.includes('verified')) {
        error.message = 'Please verify your email address to continue.';
      }
    }

    // Handle 429 Too Many Requests
    if (error.response?.status === 429) {
      error.message = 'Too many requests. Please try again later.';
    }

    // Handle 500 Internal Server Error
    if (error.response?.status === 500) {
      error.message = 'Server error. Please try again later.';
    }

    return Promise.reject(error);
  }
);

export default apiClient;
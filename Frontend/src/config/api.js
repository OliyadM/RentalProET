/**
 * Axios API Client Configuration
 * 
 * This is the single source of truth for all HTTP requests.
 * All API calls go through this configured axios instance.
 */

import axios from 'axios';

// Create axios instance with base configuration
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
});

// Request Interceptor - Add JWT token to every request
apiClient.interceptors.request.use(
  (config) => {
    // Get user from localStorage
    const userStr = localStorage.getItem('rentalpro_user');
    
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        if (user?.token) {
          config.headers.Authorization = `Bearer ${user.token}`;
        }
      } catch (error) {
        console.error('Error parsing user from localStorage:', error);
      }
    }

    // Log request in debug mode
    if (import.meta.env.VITE_DEBUG === 'true') {
      console.log('🚀 API Request:', config.method.toUpperCase(), config.url);
    }

    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

// Response Interceptor - Handle responses and errors globally
apiClient.interceptors.response.use(
  (response) => {
    // Log response in debug mode
    if (import.meta.env.VITE_DEBUG === 'true') {
      console.log('✅ API Response:', response.config.url, response.status);
    }
    return response;
  },
  (error) => {
    // Handle different error scenarios
    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response;

      if (import.meta.env.VITE_DEBUG === 'true') {
        console.error('❌ API Error:', status, error.config.url, data);
      }

      // Handle 401 Unauthorized - Auto logout
      if (status === 401) {
        console.warn('🔒 Unauthorized - Logging out');
        localStorage.removeItem('rentalpro_user');
        
        // Redirect to login if not already there
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
      }

      // Handle 403 Forbidden
      if (status === 403) {
        console.error('🚫 Forbidden - Insufficient permissions');
      }

      // Handle 404 Not Found
      if (status === 404) {
        console.error('🔍 Not Found:', error.config.url);
      }

      // Handle 500 Server Error
      if (status >= 500) {
        console.error('🔥 Server Error:', status);
      }

    } else if (error.request) {
      // Request was made but no response received
      console.error('📡 Network Error - No response from server');
    } else {
      // Something else happened
      console.error('⚠️ Request Setup Error:', error.message);
    }

    return Promise.reject(error);
  }
);

export default apiClient;

/**
 * API Service Layer
 * 
 * This file contains all API calls to the backend.
 * Uses the configured axios instance from config/api.js
 */

import apiClient from "../config/api";

// ─── Auth ────────────────────────────────────────────────
export const authAPI = {
  login: async (email, password) => {
    const response = await apiClient.post("/auth/login", { email, password });
    return response.data;
  },
  register: async (data) => {
    const response = await apiClient.post("/auth/register", data);
    return response.data;
  },
};

// ─── Properties ──────────────────────────────────────────
export const propertiesAPI = {
  getMyProperties: async () => {
    const response = await apiClient.get("/properties/my-properties");
    return response.data;
  },
  getAll: async () => {
    const response = await apiClient.get("/properties");
    return response.data;
  },
  getById: async (id) => {
    const response = await apiClient.get(`/properties/${id}`);
    return response.data;
  },
  getBySubCity: async (subCity) => {
    const response = await apiClient.get(`/properties/subcity/${subCity}`);
    return response.data;
  },
  create: async (data) => {
    const response = await apiClient.post("/properties", data);
    return response.data;
  },
  verify: async (id) => {
    const response = await apiClient.put(`/properties/${id}/verify`);
    return response.data;
  },
};

// ─── Units ───────────────────────────────────────────────
export const unitsAPI = {
  getByProperty: async (propertyId) => {
    const response = await apiClient.get(`/units/property/${propertyId}`);
    return response.data;
  },
  getById: async (id) => {
    const response = await apiClient.get(`/units/${id}`);
    return response.data;
  },
  create: async (propertyId, data) => {
    const response = await apiClient.post(`/units/property/${propertyId}`, data);
    return response.data;
  },
};

// ─── Contracts ───────────────────────────────────────────
export const contractsAPI = {
  getLandlordContracts: async () => {
    const response = await apiClient.get("/contracts/my-contracts/landlord");
    return response.data;
  },
  getTenantContracts: async () => {
    const response = await apiClient.get("/contracts/my-contracts/tenant");
    return response.data;
  },
  getAll: async () => {
    const response = await apiClient.get("/contracts");
    return response.data;
  },
  getById: async (id) => {
    const response = await apiClient.get(`/contracts/${id}`);
    return response.data;
  },
  getByStatus: async (status) => {
    const response = await apiClient.get(`/contracts/by-status/${status}`);
    return response.data;
  },
  create: async (data) => {
    const response = await apiClient.post("/contracts", data);
    return response.data;
  },
  submit: async (id) => {
    const response = await apiClient.put(`/contracts/${id}/submit`);
    return response.data;
  },
  confirm: async (id, signature) => {
    const response = await apiClient.post(`/contracts/${id}/confirm`, { signature });
    return response.data;
  },
  reject: async (id, reason) => {
    const response = await apiClient.post(`/contracts/${id}/reject`, { reason });
    return response.data;
  },
  terminate: async (id, reason) => {
    const response = await apiClient.post(`/contracts/${id}/terminate`, null, {
      params: { reason }
    });
    return response.data;
  },
};

// ─── Declarations ─────────────────────────────────────────
export const declarationsAPI = {
  getByContract: async (contractId) => {
    const response = await apiClient.get(`/declarations/contract/${contractId}`);
    return response.data;
  },
  getAnomalies: async (subCity) => {
    const response = await apiClient.get(`/declarations/anomalies/${subCity}`);
    return response.data;
  },
  getUnverified: async (subCity) => {
    const response = await apiClient.get("/declarations/unverified", {
      params: { subCity }
    });
    return response.data;
  },
  create: async (contractId, period, declaredRent) => {
    const response = await apiClient.post(
      `/declarations/contract/${contractId}`,
      null,
      {
        params: { period, declaredRent }
      }
    );
    return response.data;
  },
  verify: async (id, notes) => {
    const response = await apiClient.put(`/declarations/${id}/verify`, null, {
      params: { notes }
    });
    return response.data;
  },
};

// ─── Appeals ─────────────────────────────────────────────
export const appealsAPI = {
  getMyAppeals: async () => {
    const response = await apiClient.get("/appeals/my-appeals");
    return response.data;
  },
  getPending: async () => {
    const response = await apiClient.get("/appeals/pending");
    return response.data;
  },
  getAll: async () => {
    const response = await apiClient.get("/appeals");
    return response.data;
  },
  getById: async (id) => {
    const response = await apiClient.get(`/appeals/${id}`);
    return response.data;
  },
  create: async (data) => {
    const response = await apiClient.post("/appeals", data);
    return response.data;
  },
  resolve: async (id, decision, notes) => {
    const response = await apiClient.post(`/appeals/${id}/resolve`, null, {
      params: { decision, notes }
    });
    return response.data;
  },
  reject: async (id, reason) => {
    const response = await apiClient.post(`/appeals/${id}/reject`, null, {
      params: { reason }
    });
    return response.data;
  },
};

// ─── Analytics ───────────────────────────────────────────
export const analyticsAPI = {
  getBenchmark: async (propertyId) => {
    const response = await apiClient.get(`/analytics/benchmark/${propertyId}`);
    return response.data;
  },
};

// ─── Admin ────────────────────────────────────────────────
export const adminAPI = {
  // System Config
  getConfig: async () => {
    const response = await apiClient.get("/admin/config");
    return response.data;
  },
  updateConfig: async (data) => {
    const response = await apiClient.put("/admin/config", data);
    return response.data;
  },
  // Officer Management
  getOfficers: async () => {
    const response = await apiClient.get("/admin/officers");
    return response.data;
  },
  createOfficer: async (data) => {
    const response = await apiClient.post("/admin/officers", data);
    return response.data;
  },
  toggleOfficerStatus: async (id, active) => {
    const response = await apiClient.put(`/admin/officers/${id}/status`, null, {
      params: { active },
    });
    return response.data;
  },
};

// ─── Notifications ────────────────────────────────────────
export const notificationsAPI = {
  getMyNotifications: async () => {
    const response = await apiClient.get("/notifications/my-notifications");
    return response.data;
  },
  getUnreadCount: async () => {
    const response = await apiClient.get("/notifications/unread-count");
    return response.data.count;
  },
  markAsRead: async (id) => {
    const response = await apiClient.put(`/notifications/${id}/read`);
    return response.data;
  },
  markAllAsRead: async () => {
    await apiClient.put("/notifications/read-all");
  },
};

// ─── Profile ──────────────────────────────────────────────
export const profileAPI = {
  getMyProfile: async () => {
    const response = await apiClient.get("/users/profile/me");
    return response.data;
  },
  updateProfile: async (data) => {
    const response = await apiClient.post("/users/profile", data);
    return response.data;
  },
  getPendingProfiles: async () => {
    const response = await apiClient.get("/users/profiles/pending");
    return response.data;
  },
  verifyProfile: async (userId, status, verificationNotes, rejectionReason) => {
    const response = await apiClient.post("/users/profiles/verify", {
      userId,
      status,
      verificationNotes,
      rejectionReason,
    });
    return response.data;
  },
};
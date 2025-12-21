import React, { useState, useEffect } from 'react';
import { Clock, Bug } from 'lucide-react';

// API Service
const API_BASE = 'http://localhost:8080/api';
const API_ROLES = 'http://localhost:8080/api/roles';

const apiService = {
  register: async (data) => {
    const response = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return response.json();
  },

  verifyEmail: async (data) => {
    const response = await fetch(`${API_BASE}/auth/verify-email`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return response.json();
  },

  login: async (data) => {
    const response = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return response.json();
  },

  getRoles: async () => {
    const response = await fetch(API_ROLES);
    return response.json();
  },

  getCompanies: async () => {
    const response = await fetch(`${API_BASE}/companies/active`);
    return response.json();
  }
};

export default apiService; 

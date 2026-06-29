import api from './axios.js'
export const login = (credentials) => api.post('/auth/login', credentials)
export const forgotPassword = (data) => api.post('/auth/forgot-password', data)

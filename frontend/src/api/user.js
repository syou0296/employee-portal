import api from './axios.js'
export const getMyProfile = () => api.get('/me')
export const updateMyProfile = (data) => api.put('/me', data)
export const changePassword = (data) => api.put('/me/password', data)

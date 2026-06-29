import api from './axios.js'
export const createEmployee = (data) => api.post('/admin/employees', data)
export const getEmployees = () => api.get('/admin/employees')
export const getEmployee = (employeeId) => api.get(`/admin/employees/${employeeId}`)
export const updateEmployee = (employeeId, data) => api.put(`/admin/employees/${employeeId}`, data)
export const terminateEmployee = (employeeId) => api.put(`/admin/employees/${employeeId}/terminate`)
export const requestBackgroundCheck = (employeeId) => api.post(`/admin/employees/${employeeId}/background-checks`)
export const getBackgroundChecks = (employeeId) => api.get(`/admin/employees/${employeeId}/background-checks`)
export const getBackgroundCheck = (employeeId, checkId) => api.get(`/admin/employees/${employeeId}/background-checks/${checkId}`)

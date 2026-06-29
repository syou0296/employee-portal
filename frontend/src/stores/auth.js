import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const role = ref(localStorage.getItem('role'))
  const employeeId = ref(localStorage.getItem('employeeId'))
  const name = ref(localStorage.getItem('name'))

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ROLE_ADMIN')

  function setAuth(data) {
    token.value = data.accessToken
    role.value = data.role
    employeeId.value = data.employeeId
    name.value = data.name
    localStorage.setItem('token', data.accessToken)
    localStorage.setItem('role', data.role)
    localStorage.setItem('employeeId', data.employeeId)
    localStorage.setItem('name', data.name)
  }

  function logout() {
    token.value = null
    role.value = null
    employeeId.value = null
    name.value = null
    localStorage.clear()
  }

  return { token, role, employeeId, name, isAuthenticated, isAdmin, setAuth, logout }
})

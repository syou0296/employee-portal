import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import LoginView from '../views/auth/LoginView.vue'
import ProfileView from '../views/user/ProfileView.vue'
import EmployeeListView from '../views/admin/EmployeeListView.vue'
import EmployeeCreateView from '../views/admin/EmployeeCreateView.vue'
import EmployeeDetailView from '../views/admin/EmployeeDetailView.vue'
import BackgroundCheckView from '../views/admin/BackgroundCheckView.vue'
import UserLayout from '../layouts/UserLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'

const routes = [
  { path: '/login', component: LoginView },
  {
    path: '/',
    component: UserLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/profile' },
      { path: 'profile', component: ProfileView, meta: { requiresAuth: true } }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/employees' },
      { path: 'employees', component: EmployeeListView },
      { path: 'employees/new', component: EmployeeCreateView },
      { path: 'employees/:employeeId', component: EmployeeDetailView },
      { path: 'employees/:employeeId/background-checks/:checkId', component: BackgroundCheckView }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) return '/login'
  if (to.meta.requiresAdmin && !auth.isAdmin) return '/profile'
  if (to.path === '/login' && auth.isAuthenticated) {
    return auth.isAdmin ? '/admin/employees' : '/profile'
  }
})

export default router

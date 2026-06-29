<template>
  <div class="layout">
    <header class="navbar">
      <div class="navbar-brand">
        <span class="brand-icon">HR</span>
        HR Portal
      </div>
      <div class="navbar-right">
        <span class="user-name">{{ auth.name }}</span>
        <button class="btn btn-outline" @click="handleLogout">로그아웃</button>
      </div>
    </header>
    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const auth = useAuthStore()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  background: white;
  border-bottom: 1px solid #E5E7EB;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.brand-icon {
  background: #4F46E5;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 800;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-name {
  font-size: 14px;
  color: #6B7280;
}

.main-content {
  flex: 1;
  padding: 32px 24px;
  max-width: 960px;
  margin: 0 auto;
  width: 100%;
}
</style>

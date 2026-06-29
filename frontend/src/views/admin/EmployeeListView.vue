<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">직원 관리</h1>
      <button class="btn btn-primary" @click="router.push('/admin/employees/new')">
        + 직원 추가
      </button>
    </div>

    <div v-if="isLoading" class="loading-spinner">
      <div class="spinner"></div>
      <span>불러오는 중...</span>
    </div>

    <div v-else-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <div v-else class="card" style="padding:0;overflow:hidden;">
      <table>
        <thead>
          <tr>
            <th>직원 ID</th>
            <th>이름</th>
            <th>이메일</th>
            <th>부서</th>
            <th>직급</th>
            <th>상태</th>
            <th>입사일</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="emp in employees"
            :key="emp.employeeId"
            :style="emp.status === 'TERMINATED' ? 'opacity:0.6' : ''"
          >
            <td><code style="font-size:12px;">{{ emp.employeeId }}</code></td>
            <td style="font-weight:500;">{{ emp.lastName }} {{ emp.firstName }}</td>
            <td style="color:#6B7280;">{{ emp.email }}</td>
            <td>{{ emp.department || '-' }}</td>
            <td>{{ emp.position || '-' }}</td>
            <td>
              <span :class="['badge', emp.status === 'ACTIVE' ? 'badge-success' : 'badge-danger']">
                {{ emp.status === 'ACTIVE' ? '재직 중' : '퇴사' }}
              </span>
            </td>
            <td style="color:#6B7280;font-size:13px;">{{ emp.hireDate }}</td>
            <td>
              <button
                class="btn btn-outline"
                style="padding:4px 12px;font-size:13px;"
                @click="router.push('/admin/employees/' + emp.employeeId)"
              >
                상세보기
              </button>
            </td>
          </tr>
          <tr v-if="employees.length === 0">
            <td colspan="8" style="text-align:center;color:#9CA3AF;padding:40px;">
              등록된 직원이 없습니다.
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getEmployees } from '../../api/admin.js'

const router = useRouter()
const employees = ref([])
const isLoading = ref(false)
const errorMessage = ref('')

onMounted(async () => {
  isLoading.value = true
  try {
    const res = await getEmployees()
    employees.value = res.data.data
  } catch (err) {
    errorMessage.value = err?.message || '직원 목록을 불러오는 데 실패했습니다.'
  } finally {
    isLoading.value = false
  }
})
</script>

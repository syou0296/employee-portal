<template>
  <div>
    <div class="page-header">
      <div style="display:flex;align-items:center;gap:12px;">
        <button class="btn btn-secondary" @click="router.back()" style="padding:6px 12px;">← 뒤로</button>
        <h1 class="page-title">Background Check 결과</h1>
      </div>
      <button v-if="result && result.status === 'pending'" class="btn btn-outline" @click="pollNow" :disabled="isPolling">
        새로고침
      </button>
    </div>

    <div v-if="isLoading" class="loading-spinner">
      <div class="spinner"></div>
      <span>불러오는 중...</span>
    </div>

    <template v-else-if="result">
      <div v-if="result.status === 'pending'" class="card polling-card">
        <div class="polling-icon">
          <div class="spinner" style="width:32px;height:32px;border-width:3px;"></div>
        </div>
        <h3 class="polling-title">처리 중입니다...</h3>
        <p class="polling-desc">Background Check가 진행 중입니다. 잠시 후 자동으로 결과가 업데이트됩니다.</p>
        <p class="polling-counter" v-if="pollCount > 0">자동 확인 {{ pollCount }}/12회 완료</p>
      </div>

      <div v-if="timeoutMessage" class="alert alert-warning" style="background:#FFFBEB;color:#92400E;border:1px solid #FDE68A;">
        {{ timeoutMessage }}
      </div>

      <div v-if="result.status !== 'PENDING'" class="card">
        <div class="result-header">
          <div>
            <p style="font-size:13px;color:#6B7280;margin-bottom:4px;">Check ID</p>
            <code style="font-size:13px;">{{ result.checkId }}</code>
          </div>
          <span :class="['status-badge', result.status === 'clear' ? 'status-clear' : result.status === 'flagged' ? 'status-flagged' : 'status-pending']">
            {{ getStatusLabel(result.status) }}
          </span>
        </div>

        <div class="result-info" style="margin-bottom:24px;">
          <div class="info-item">
            <span class="info-label">직원명</span>
            <span class="info-value">{{ result.lastName }} {{ result.firstName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">요청일</span>
            <span class="info-value">{{ formatDate(result.createdAt) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">완료일</span>
            <span class="info-value">{{ result.completedAt ? formatDate(result.completedAt) : '-' }}</span>
          </div>
        </div>

        <hr style="border:none;border-top:1px solid #F3F4F6;margin-bottom:24px;" />

        <h3 style="font-size:15px;font-weight:600;color:#374151;margin-bottom:16px;">상세 결과</h3>
        <div class="check-items">
          <div class="check-item">
            <div class="check-label">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
              범죄 이력
            </div>
            <span :class="['badge', result.criminalRecord ? 'badge-danger' : 'badge-success']">
              {{ result.criminalRecord ? '있음' : '없음' }}
            </span>
          </div>
          <div class="check-item">
            <div class="check-label">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 10v6M2 10l10-5 10 5-10 5z"/>
                <path d="M6 12v5c3 3 9 3 12 0v-5"/>
              </svg>
              학력 검증
            </div>
            <span :class="['badge', result.educationVerified ? 'badge-success' : 'badge-warning']">
              {{ result.educationVerified ? '확인됨' : '미확인' }}
            </span>
          </div>
          <div class="check-item">
            <div class="check-label">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="7" width="20" height="14" rx="2" ry="2"/>
                <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
              </svg>
              경력 검증
            </div>
            <span :class="['badge', result.employmentVerified ? 'badge-success' : 'badge-warning']">
              {{ result.employmentVerified ? '확인됨' : '미확인' }}
            </span>
          </div>
          <div class="check-item">
            <div class="check-label">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="1" x2="12" y2="23"/>
                <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
              </svg>
              신용 등급
            </div>
            <span :class="['badge', getCreditBadge(result.creditScore)]">
              {{ getCreditLabel(result.creditScore) }}
            </span>
          </div>
        </div>
      </div>
    </template>

    <div v-else-if="!isLoading && errorMessage" class="alert alert-error">{{ errorMessage }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getBackgroundCheck } from '../../api/admin.js'

const router = useRouter()
const route = useRoute()
const employeeId = route.params.employeeId
const checkId = route.params.checkId

const result = ref(null)
const isLoading = ref(false)
const isPolling = ref(false)
const errorMessage = ref('')
const timeoutMessage = ref('')
const pollCount = ref(0)
let pollTimer = null
const MAX_POLLS = 12

onMounted(async () => {
  await loadResult()
  if (result.value?.status === 'pending') {
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
})

async function loadResult() {
  isLoading.value = true
  try {
    const res = await getBackgroundCheck(employeeId, checkId)
    result.value = res.data.data
  } catch (err) {
    errorMessage.value = err?.message || '결과를 불러오는 데 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

async function pollNow() {
  isPolling.value = true
  try {
    const res = await getBackgroundCheck(employeeId, checkId)
    result.value = res.data.data
    if (result.value.status !== 'pending') {
      stopPolling()
    }
  } catch {
    // ignore poll errors
  } finally {
    isPolling.value = false
  }
}

function startPolling() {
  pollTimer = setInterval(async () => {
    if (pollCount.value >= MAX_POLLS) {
      stopPolling()
      timeoutMessage.value = '처리 시간이 길어지고 있습니다. 나중에 다시 확인해주세요.'
      return
    }
    pollCount.value++
    try {
      const res = await getBackgroundCheck(employeeId, checkId)
      result.value = res.data.data
      if (result.value.status !== 'pending') {
        stopPolling()
      }
    } catch {
      // ignore poll errors
    }
  }, 5000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function getStatusLabel(status) {
  const map = { pending: '처리 중', clear: '이상 없음', flagged: '주의 필요' }
  return map[status] || status
}

function getCreditBadge(score) {
  const map = { excellent: 'badge-success', good: 'badge-success', fair: 'badge-warning', poor: 'badge-danger' }
  return map[score] || 'badge-gray'
}

function getCreditLabel(score) {
  const map = { excellent: '매우 좋음 (Excellent)', good: '좋음 (Good)', fair: '보통 (Fair)', poor: '나쁨 (Poor)' }
  return map[score] || score || '-'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped>
.polling-card {
  text-align: center;
  padding: 48px 24px;
  margin-bottom: 24px;
}
.polling-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}
.polling-title { font-size: 18px; font-weight: 600; color: #374151; margin-bottom: 8px; }
.polling-desc { font-size: 14px; color: #6B7280; margin-bottom: 8px; }
.polling-counter { font-size: 13px; color: #9CA3AF; }

.result-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 18px;
  border-radius: 99px;
  font-size: 14px;
  font-weight: 600;
}

.status-clear { background: #D1FAE5; color: #065F46; }
.status-flagged { background: #FEE2E2; color: #991B1B; }
.status-pending { background: #FEF3C7; color: #92400E; }

.result-info {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: 12px; color: #6B7280; font-weight: 500; text-transform: uppercase; letter-spacing: 0.05em; }
.info-value { font-size: 14px; color: #111827; font-weight: 500; }

.check-items { display: flex; flex-direction: column; gap: 16px; }
.check-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #F9FAFB;
  border-radius: 8px;
  border: 1px solid #E5E7EB;
}
.check-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}
</style>

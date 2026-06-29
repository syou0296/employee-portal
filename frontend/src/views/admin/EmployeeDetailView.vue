<template>
  <div>
    <div class="page-header">
      <div style="display:flex;align-items:center;gap:12px;">
        <button class="btn btn-secondary" @click="router.push('/admin/employees')" style="padding:6px 12px;">← 목록</button>
        <h1 class="page-title">직원 상세</h1>
      </div>
      <div v-if="employee" style="display:flex;gap:8px;">
        <button
          v-if="employee.role !== 'ROLE_ADMIN'"
          class="btn btn-danger"
          :disabled="employee.status !== 'ACTIVE' || isTerminating"
          @click="handleTerminate"
        >
          <span v-if="isTerminating" class="spinner" style="width:14px;height:14px;margin-right:6px;"></span>
          {{ isTerminating ? '처리 중...' : '퇴사 처리' }}
        </button>
        <button
          class="btn btn-primary"
          :disabled="isRequestingBgCheck"
          @click="handleBgCheckRequest"
        >
          <span v-if="isRequestingBgCheck" class="spinner" style="width:14px;height:14px;margin-right:6px;"></span>
          {{ isRequestingBgCheck ? '요청 중...' : 'Background Check 요청' }}
        </button>
      </div>
    </div>

    <div v-if="isLoading" class="loading-spinner">
      <div class="spinner"></div>
      <span>불러오는 중...</span>
    </div>

    <template v-else-if="employee">
      <div v-if="actionMessage" :class="['alert', actionMessageType === 'success' ? 'alert-success' : 'alert-error']">
        {{ actionMessage }}
      </div>

      <div class="card" style="margin-bottom:24px;">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px;">
          <div>
            <h2 style="font-size:20px;font-weight:700;color:#111827;">{{ employee.lastName }} {{ employee.firstName }}</h2>
            <p style="font-size:14px;color:#6B7280;margin-top:4px;">{{ employee.email }}</p>
          </div>
          <span :class="['badge', employee.status === 'ACTIVE' ? 'badge-success' : 'badge-danger']" style="font-size:13px;padding:4px 14px;">
            {{ employee.status === 'ACTIVE' ? '재직 중' : '퇴사' }}
          </span>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">직원 ID</span>
            <span class="info-value"><code>{{ employee.employeeId }}</code></span>
          </div>
          <div class="info-item">
            <span class="info-label">사용자명</span>
            <span class="info-value">{{ employee.username }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">생년월일</span>
            <span class="info-value">{{ employee.dateOfBirth }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">전화번호</span>
            <span class="info-value">{{ employee.phone || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">부서</span>
            <span class="info-value">{{ employee.department || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">직급/포지션</span>
            <span class="info-value">{{ employee.position || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">권한</span>
            <span class="info-value">{{ employee.role === 'ROLE_ADMIN' ? '관리자' : '일반 사용자' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">입사일</span>
            <span class="info-value">{{ employee.hireDate }}</span>
          </div>
          <div v-if="employee.terminationDate" class="info-item">
            <span class="info-label">퇴사일</span>
            <span class="info-value">{{ employee.terminationDate }}</span>
          </div>
        </div>
      </div>

      <!-- 관리자 정보 수정 섹션 -->
      <div class="card" style="margin-bottom:24px;">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
          <h3 style="font-size:16px;font-weight:600;color:#111827;">직원 정보 수정 <span style="font-size:12px;color:#6B7280;font-weight:400;">(관리자 전용)</span></h3>
          <div style="display:flex;gap:8px;">
            <button v-if="!isEditingInfo" class="btn btn-secondary" @click="startEditInfo">수정</button>
            <template v-else>
              <button class="btn btn-secondary" @click="cancelEditInfo" :disabled="isSavingInfo">취소</button>
              <button class="btn btn-primary" @click="handleSaveInfo" :disabled="isSavingInfo">
                <span v-if="isSavingInfo" class="spinner" style="width:14px;height:14px;margin-right:6px;"></span>
                {{ isSavingInfo ? '저장 중...' : '저장' }}
              </button>
            </template>
          </div>
        </div>
        <div class="form-grid">
          <div class="form-group">
            <label>성 (Last Name)</label>
            <input v-model="infoForm.lastName" type="text" :readonly="!isEditingInfo" :class="{ readonly: !isEditingInfo }" />
          </div>
          <div class="form-group">
            <label>이름 (First Name)</label>
            <input v-model="infoForm.firstName" type="text" :readonly="!isEditingInfo" :class="{ readonly: !isEditingInfo }" />
          </div>
          <div class="form-group">
            <label>이메일</label>
            <input v-model="infoForm.email" type="email" :readonly="!isEditingInfo" :class="{ readonly: !isEditingInfo, error: infoFieldErrors.email }" />
            <span v-if="infoFieldErrors.email" class="field-error">{{ infoFieldErrors.email }}</span>
          </div>
          <div class="form-group">
            <label>전화번호</label>
            <input v-model="infoForm.phone" type="text" :readonly="!isEditingInfo" :class="{ readonly: !isEditingInfo }" />
          </div>
          <div class="form-group">
            <label>부서</label>
            <template v-if="isEditingInfo">
              <select v-model="infoForm.department">
                <option value="">선택 안 함</option>
                <option value="개발팀">Engineering</option>
                <option value="인사팀">Human Resources</option>
                <option value="영업팀">Sales</option>
              </select>
            </template>
            <input v-else :value="infoForm.department" readonly class="readonly" />
          </div>
          <div class="form-group">
            <label>직급/포지션</label>
            <template v-if="isEditingInfo">
              <select v-model="infoForm.position">
                <option value="">선택 안 함</option>
                <option value="사원">Staff</option>
                <option value="대리">Assistant</option>
                <option value="과장">Manager</option>
              </select>
            </template>
            <input v-else :value="infoForm.position" readonly class="readonly" />
          </div>
          <div class="form-group">
            <label>생년월일</label>
            <input v-model="infoForm.dateOfBirth" type="date" :readonly="!isEditingInfo" :class="{ readonly: !isEditingInfo }" />
          </div>
          <div class="form-group">
            <label>입사일</label>
            <input v-model="infoForm.hireDate" type="date" :readonly="!isEditingInfo" :class="{ readonly: !isEditingInfo }" />
          </div>
        </div>
      </div>

      <!-- 관리자 비밀번호 변경 섹션 -->
      <div class="card" style="margin-bottom:24px;">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
          <h3 style="font-size:16px;font-weight:600;color:#111827;">비밀번호 변경 <span style="font-size:12px;color:#6B7280;font-weight:400;">(관리자 전용)</span></h3>
          <div style="display:flex;gap:8px;">
            <button v-if="!isEditingPw" class="btn btn-secondary" @click="isEditingPw = true">변경</button>
            <template v-else>
              <button class="btn btn-secondary" @click="cancelEditPw" :disabled="isSavingPw">취소</button>
              <button class="btn btn-primary" @click="handleSavePw" :disabled="isSavingPw">
                <span v-if="isSavingPw" class="spinner" style="width:14px;height:14px;margin-right:6px;"></span>
                {{ isSavingPw ? '저장 중...' : '저장' }}
              </button>
            </template>
          </div>
        </div>
        <template v-if="isEditingPw">
          <div class="form-grid" style="max-width:480px;">
            <div class="form-group">
              <label>새 비밀번호</label>
              <input v-model="pwForm.newPassword" type="password" :class="{ error: pwFieldErrors.newPassword }" placeholder="8자 이상, 영문+숫자 포함" />
              <span class="field-hint">8자 이상, 영문+숫자 포함</span>
              <span v-if="pwFieldErrors.newPassword" class="field-error">{{ pwFieldErrors.newPassword }}</span>
            </div>
            <div class="form-group">
              <label>새 비밀번호 확인</label>
              <input v-model="pwForm.confirmPassword" type="password" :class="{ error: pwFieldErrors.confirmPassword }" placeholder="비밀번호를 다시 입력하세요" />
              <span v-if="pwFieldErrors.confirmPassword" class="field-error">{{ pwFieldErrors.confirmPassword }}</span>
            </div>
          </div>
        </template>
        <p v-else style="font-size:14px;color:#9CA3AF;">변경 버튼을 눌러 직원의 비밀번호를 재설정할 수 있습니다.</p>
      </div>

      <div class="card" style="padding:0;overflow:hidden;">
        <div style="padding:20px 24px;border-bottom:1px solid #E5E7EB;">
          <h3 style="font-size:16px;font-weight:600;color:#111827;">Background Check 이력</h3>
        </div>

        <div v-if="isLoadingChecks" class="loading-spinner" style="padding:32px;">
          <div class="spinner"></div>
          <span>이력 불러오는 중...</span>
        </div>

        <table v-else>
          <thead>
            <tr>
              <th>Check ID</th>
              <th>상태</th>
              <th>요청일</th>
              <th>완료일</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="check in backgroundChecks"
              :key="check.checkId"
              style="cursor:pointer;"
              @click="router.push('/admin/employees/' + employeeId + '/background-checks/' + check.checkId)"
            >
              <td><code style="font-size:12px;">{{ check.checkId }}</code></td>
              <td>
                <span :class="['badge', getStatusBadge(check.status)]">{{ getStatusLabel(check.status) }}</span>
              </td>
              <td style="font-size:13px;color:#6B7280;">{{ formatDate(check.createdAt) }}</td>
              <td style="font-size:13px;color:#6B7280;">{{ check.completedAt ? formatDate(check.completedAt) : '-' }}</td>
            </tr>
            <tr v-if="backgroundChecks.length === 0">
              <td colspan="4" style="text-align:center;color:#9CA3AF;padding:32px;">
                Background Check 이력이 없습니다.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <div v-else-if="!isLoading" class="alert alert-error">직원 정보를 불러올 수 없습니다.</div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getEmployee, updateEmployee, terminateEmployee, requestBackgroundCheck, getBackgroundChecks } from '../../api/admin.js'

const router = useRouter()
const route = useRoute()
const employeeId = route.params.employeeId

const employee = ref(null)
const backgroundChecks = ref([])
const isLoading = ref(false)
const isLoadingChecks = ref(false)
const isTerminating = ref(false)
const isRequestingBgCheck = ref(false)
const actionMessage = ref('')
const actionMessageType = ref('success')

// 관리자 정보 수정
const isEditingInfo = ref(false)
const isSavingInfo = ref(false)
const infoForm = reactive({ firstName: '', lastName: '', email: '', phone: '', department: '', position: '', dateOfBirth: '', hireDate: '' })
const infoFieldErrors = reactive({})

// 비밀번호 변경
const isEditingPw = ref(false)
const isSavingPw = ref(false)
const pwForm = reactive({ newPassword: '', confirmPassword: '' })
const pwFieldErrors = reactive({})

onMounted(async () => {
  await Promise.all([loadEmployee(), loadBackgroundChecks()])
})

async function loadEmployee() {
  isLoading.value = true
  try {
    const res = await getEmployee(employeeId)
    employee.value = res.data.data
    syncInfoForm()
  } catch (err) {
    setMessage(err?.message || '직원 정보를 불러오는 데 실패했습니다.', 'error')
  } finally {
    isLoading.value = false
  }
}

function syncInfoForm() {
  if (!employee.value) return
  infoForm.firstName   = employee.value.firstName || ''
  infoForm.lastName    = employee.value.lastName || ''
  infoForm.email       = employee.value.email || ''
  infoForm.phone       = employee.value.phone || ''
  infoForm.department  = employee.value.department || ''
  infoForm.position    = employee.value.position || ''
  infoForm.dateOfBirth = employee.value.dateOfBirth || ''
  infoForm.hireDate    = employee.value.hireDate || ''
}

function startEditInfo() {
  syncInfoForm()
  Object.keys(infoFieldErrors).forEach(k => delete infoFieldErrors[k])
  isEditingInfo.value = true
}

function cancelEditInfo() {
  syncInfoForm()
  Object.keys(infoFieldErrors).forEach(k => delete infoFieldErrors[k])
  isEditingInfo.value = false
}

function cancelEditPw() {
  pwForm.newPassword = ''
  pwForm.confirmPassword = ''
  Object.keys(pwFieldErrors).forEach(k => delete pwFieldErrors[k])
  isEditingPw.value = false
}

function validatePassword(pw) {
  if (!pw || pw.length < 8) return '비밀번호는 8자 이상이어야 합니다.'
  if (!/[A-Za-z]/.test(pw)) return '영문을 포함해야 합니다.'
  if (!/\d/.test(pw)) return '숫자를 포함해야 합니다.'
  return ''
}

async function handleSavePw() {
  Object.keys(pwFieldErrors).forEach(k => delete pwFieldErrors[k])
  const pwError = validatePassword(pwForm.newPassword)
  if (pwError) { pwFieldErrors.newPassword = pwError; return }
  if (pwForm.newPassword !== pwForm.confirmPassword) {
    pwFieldErrors.confirmPassword = '비밀번호가 일치하지 않습니다.'
    return
  }
  isSavingPw.value = true
  try {
    await updateEmployee(employeeId, { newPassword: pwForm.newPassword })
    cancelEditPw()
    setMessage('비밀번호가 변경되었습니다.', 'success')
  } catch (err) {
    const errData = err?.response?.data?.error
    pwFieldErrors.newPassword = errData?.message || '비밀번호 변경에 실패했습니다.'
  } finally {
    isSavingPw.value = false
  }
}

async function handleSaveInfo() {
  Object.keys(infoFieldErrors).forEach(k => delete infoFieldErrors[k])
  if (!infoForm.email) {
    infoFieldErrors.email = '이메일을 입력해주세요.'
    return
  }
  isSavingInfo.value = true
  try {
    const res = await updateEmployee(employeeId, {
      firstName:   infoForm.firstName   || null,
      lastName:    infoForm.lastName    || null,
      email:       infoForm.email       || null,
      phone:       infoForm.phone       || null,
      department:  infoForm.department  || null,
      position:    infoForm.position    || null,
      dateOfBirth: infoForm.dateOfBirth || null,
      hireDate:    infoForm.hireDate    || null,
    })
    employee.value = res.data.data
    syncInfoForm()
    isEditingInfo.value = false
    setMessage('직원 정보가 수정되었습니다.', 'success')
  } catch (err) {
    const errData = err?.response?.data?.error
    if (errData?.fieldErrors) {
      errData.fieldErrors.forEach(fe => { infoFieldErrors[fe.field] = fe.message })
    } else {
      setMessage(errData?.message || '수정에 실패했습니다.', 'error')
    }
  } finally {
    isSavingInfo.value = false
  }
}

async function loadBackgroundChecks() {
  isLoadingChecks.value = true
  try {
    const res = await getBackgroundChecks(employeeId)
    backgroundChecks.value = res.data.data?.checks || []
  } catch {
    backgroundChecks.value = []
  } finally {
    isLoadingChecks.value = false
  }
}

async function handleTerminate() {
  if (!confirm(`${employee.value.lastName} ${employee.value.firstName} 직원을 퇴사 처리하시겠습니까?`)) return
  isTerminating.value = true
  try {
    const res = await terminateEmployee(employeeId)
    employee.value = res.data.data
    setMessage('퇴사 처리되었습니다.', 'success')
  } catch (err) {
    setMessage(err?.response?.data?.error?.message || '퇴사 처리에 실패했습니다.', 'error')
  } finally {
    isTerminating.value = false
  }
}

async function handleBgCheckRequest() {
  isRequestingBgCheck.value = true
  try {
    await requestBackgroundCheck(employeeId)
    setMessage('Background Check가 요청되었습니다.', 'success')
    await loadBackgroundChecks()
  } catch (err) {
    setMessage(err?.response?.data?.error?.message || 'Background Check 요청에 실패했습니다.', 'error')
  } finally {
    isRequestingBgCheck.value = false
  }
}

function setMessage(msg, type = 'success') {
  actionMessage.value = msg
  actionMessageType.value = type
  setTimeout(() => { actionMessage.value = '' }, 4000)
}

function getStatusBadge(status) {
  const map = {
    pending: 'badge-warning',
    clear: 'badge-success',
    flagged: 'badge-danger'
  }
  return map[status] || 'badge-gray'
}

function getStatusLabel(status) {
  const map = {
    pending: '대기',
    clear: '이상 없음',
    flagged: '주의 필요'
  }
  return map[status] || status
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('ko-KR')
}
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #6B7280;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-value {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.form-group input.readonly {
  background: #F9FAFB;
  color: #6B7280;
  cursor: default;
}
.field-hint { font-size: 12px; color: #9CA3AF; margin-top: 4px; display: block; }
</style>

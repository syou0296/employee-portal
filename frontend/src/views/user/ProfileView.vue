<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">내 프로필</h1>
      <button v-if="!isEditing" class="btn btn-primary" @click="startEdit">수정</button>
      <div v-else style="display:flex;gap:8px;">
        <button class="btn btn-secondary" @click="cancelEdit" :disabled="isSaving">취소</button>
        <button class="btn btn-primary" @click="handleSave" :disabled="isSaving">
          <span v-if="isSaving" class="spinner" style="width:14px;height:14px;margin-right:6px;"></span>
          {{ isSaving ? '저장 중...' : '저장' }}
        </button>
      </div>
    </div>

    <div v-if="isLoading" class="loading-spinner">
      <div class="spinner"></div>
      <span>불러오는 중...</span>
    </div>

    <template v-else-if="profile">
      <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>
      <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

      <!-- 기본 정보 + 연락처 카드 -->
      <div class="card">
        <div class="profile-section">
          <h2 class="section-title">기본 정보</h2>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">직원 ID</span>
              <span class="info-value">{{ profile.employeeId }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">이름</span>
              <span class="info-value">{{ profile.lastName }} {{ profile.firstName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">사용자명</span>
              <span class="info-value">{{ profile.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">생년월일</span>
              <span class="info-value">{{ profile.dateOfBirth }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">입사일</span>
              <span class="info-value">{{ profile.hireDate }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">상태</span>
              <span :class="['badge', profile.status === 'ACTIVE' ? 'badge-success' : 'badge-danger']">
                {{ profile.status === 'ACTIVE' ? '재직 중' : '퇴사' }}
              </span>
            </div>
          </div>
        </div>

        <hr class="divider" />

        <div class="profile-section">
          <h2 class="section-title">연락처 및 직무</h2>
          <div class="form-grid">
            <div class="form-group">
              <label>이메일 <span class="readonly-badge">관리자만 수정</span></label>
              <input type="email" :value="profile.email" readonly class="readonly" />
            </div>
            <div class="form-group">
              <label>전화번호</label>
              <input
                v-model="editForm.phone"
                type="text"
                :readonly="!isEditing"
                :class="{ error: fieldErrors.phone }"
              />
              <span v-if="fieldErrors.phone" class="field-error">{{ fieldErrors.phone }}</span>
            </div>
            <div class="form-group">
              <label>부서 <span class="readonly-badge">관리자만 수정</span></label>
              <input type="text" :value="profile.department" readonly class="readonly" />
            </div>
            <div class="form-group">
              <label>직급/포지션 <span class="readonly-badge">관리자만 수정</span></label>
              <input type="text" :value="profile.position" readonly class="readonly" />
            </div>
          </div>
        </div>
      </div>

      <!-- 비밀번호 변경 카드 -->
      <div class="card" style="margin-top:24px;">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
          <h2 class="section-title" style="margin-bottom:0;">비밀번호 변경</h2>
          <button v-if="!isChangingPw" class="btn btn-secondary" @click="openPwForm">변경</button>
        </div>

        <template v-if="isChangingPw">
          <div v-if="pwErrorMessage" class="alert alert-error" style="margin-bottom:16px;">{{ pwErrorMessage }}</div>

          <div class="pw-form">
            <div class="form-group">
              <label>현재 비밀번호</label>
              <input
                v-model="pwForm.currentPassword"
                type="password"
                placeholder="현재 비밀번호 입력"
                :class="{ error: pwErrors.currentPassword }"
                autocomplete="current-password"
              />
              <span v-if="pwErrors.currentPassword" class="field-error">{{ pwErrors.currentPassword }}</span>
            </div>
            <div class="form-group">
              <label>새 비밀번호</label>
              <input
                v-model="pwForm.newPassword"
                type="password"
                placeholder="영문·숫자·특수문자 포함 8~20자"
                :class="{ error: pwErrors.newPassword }"
                autocomplete="new-password"
              />
              <span v-if="pwErrors.newPassword" class="field-error">{{ pwErrors.newPassword }}</span>
              <span v-else class="field-hint">영문, 숫자, 특수문자를 각각 1자 이상 포함한 8~20자</span>
            </div>
            <div class="form-group">
              <label>새 비밀번호 확인</label>
              <input
                v-model="pwForm.confirmPassword"
                type="password"
                placeholder="새 비밀번호 재입력"
                :class="{ error: pwErrors.confirmPassword || (pwForm.confirmPassword && pwForm.newPassword !== pwForm.confirmPassword) }"
                autocomplete="new-password"
              />
              <span v-if="pwErrors.confirmPassword" class="field-error">{{ pwErrors.confirmPassword }}</span>
              <span
                v-else-if="pwForm.confirmPassword && pwForm.newPassword !== pwForm.confirmPassword"
                class="field-error"
              >새 비밀번호와 일치하지 않습니다.</span>
              <span
                v-else-if="pwForm.confirmPassword && pwForm.newPassword === pwForm.confirmPassword"
                class="field-hint match"
              >✓ 비밀번호가 일치합니다.</span>
            </div>
          </div>

          <div style="display:flex;gap:8px;margin-top:20px;">
            <button class="btn btn-secondary" @click="closePwForm" :disabled="isPwSaving">취소</button>
            <button class="btn btn-primary" @click="handleChangePassword" :disabled="isPwSaving">
              <span v-if="isPwSaving" class="spinner" style="width:14px;height:14px;margin-right:6px;"></span>
              {{ isPwSaving ? '변경 중...' : '비밀번호 변경' }}
            </button>
          </div>
        </template>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyProfile, updateMyProfile, changePassword } from '../../api/user.js'
import { useAuthStore } from '../../stores/auth.js'

const router = useRouter()
const auth = useAuthStore()

// ── 프로필 조회/수정 ──────────────────────────────────────
const profile = ref(null)
const isLoading = ref(false)
const isEditing = ref(false)
const isSaving = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const fieldErrors = reactive({})
const editForm = reactive({ phone: '' })

onMounted(async () => {
  await loadProfile()
})

async function loadProfile() {
  isLoading.value = true
  try {
    const res = await getMyProfile()
    profile.value = res.data.data
    syncForm()
  } catch (err) {
    errorMessage.value = err?.message || '프로필을 불러오는 데 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

function syncForm() {
  if (!profile.value) return
  editForm.phone = profile.value.phone || ''
}

function startEdit() {
  syncForm()
  isEditing.value = true
  successMessage.value = ''
  errorMessage.value = ''
  Object.keys(fieldErrors).forEach(k => delete fieldErrors[k])
}

function cancelEdit() {
  isEditing.value = false
  syncForm()
  errorMessage.value = ''
  Object.keys(fieldErrors).forEach(k => delete fieldErrors[k])
}

async function handleSave() {
  isSaving.value = true
  errorMessage.value = ''
  successMessage.value = ''
  Object.keys(fieldErrors).forEach(k => delete fieldErrors[k])
  try {
    const res = await updateMyProfile({ ...editForm })
    profile.value = res.data.data
    isEditing.value = false
    successMessage.value = '프로필이 성공적으로 저장되었습니다.'
    setTimeout(() => { successMessage.value = '' }, 3000)
  } catch (err) {
    if (err?.fieldErrors) {
      err.fieldErrors.forEach(fe => { fieldErrors[fe.field] = fe.message })
    } else {
      errorMessage.value = err?.message || '저장에 실패했습니다.'
    }
  } finally {
    isSaving.value = false
  }
}

// ── 비밀번호 변경 ─────────────────────────────────────────
const isChangingPw = ref(false)
const isPwSaving = ref(false)
const pwErrorMessage = ref('')
const pwErrors = reactive({})
const pwForm = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })

const PW_REGEX = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,20}$/

function openPwForm() {
  isChangingPw.value = true
  pwErrorMessage.value = ''
  Object.assign(pwForm, { currentPassword: '', newPassword: '', confirmPassword: '' })
  Object.keys(pwErrors).forEach(k => delete pwErrors[k])
}

function closePwForm() {
  isChangingPw.value = false
  pwErrorMessage.value = ''
  Object.keys(pwErrors).forEach(k => delete pwErrors[k])
}

function validatePwForm() {
  Object.keys(pwErrors).forEach(k => delete pwErrors[k])
  let valid = true
  if (!pwForm.currentPassword) {
    pwErrors.currentPassword = '현재 비밀번호를 입력해주세요.'
    valid = false
  }
  if (!pwForm.newPassword) {
    pwErrors.newPassword = '새 비밀번호를 입력해주세요.'
    valid = false
  } else if (!PW_REGEX.test(pwForm.newPassword)) {
    pwErrors.newPassword = '영문, 숫자, 특수문자를 각각 1자 이상 포함한 8~20자로 입력해주세요.'
    valid = false
  }
  if (!pwForm.confirmPassword) {
    pwErrors.confirmPassword = '새 비밀번호 확인을 입력해주세요.'
    valid = false
  } else if (pwForm.newPassword !== pwForm.confirmPassword) {
    pwErrors.confirmPassword = '새 비밀번호와 일치하지 않습니다.'
    valid = false
  }
  return valid
}

async function handleChangePassword() {
  if (!validatePwForm()) return
  isPwSaving.value = true
  pwErrorMessage.value = ''
  try {
    await changePassword({ ...pwForm })
    successMessage.value = '비밀번호가 변경되었습니다. 다시 로그인해주세요.'
    closePwForm()
    setTimeout(() => {
      auth.logout()
      router.push('/login')
    }, 2000)
  } catch (err) {
    if (err?.fieldErrors) {
      err.fieldErrors.forEach(fe => { pwErrors[fe.field] = fe.message })
    } else {
      pwErrorMessage.value = err?.message || '비밀번호 변경에 실패했습니다.'
    }
  } finally {
    isPwSaving.value = false
  }
}
</script>

<style scoped>
.profile-section { margin-bottom: 8px; }
.section-title { font-size: 16px; font-weight: 600; color: #374151; margin-bottom: 16px; }
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: 12px; color: #6B7280; font-weight: 500; text-transform: uppercase; letter-spacing: 0.05em; }
.info-value { font-size: 15px; color: #111827; font-weight: 500; }
.divider { border: none; border-top: 1px solid #F3F4F6; margin: 24px 0; }
.form-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 0 24px; }
.readonly { background: #F9FAFB; color: #6B7280; cursor: not-allowed; }
.readonly-badge { font-size: 10px; font-weight: 500; color: #9CA3AF; background: #F3F4F6; border-radius: 4px; padding: 1px 6px; margin-left: 6px; vertical-align: middle; }
.pw-form { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 0 24px; }
.field-hint { font-size: 12px; color: #9CA3AF; margin-top: 4px; display: block; }
.field-hint.match { color: #10B981; font-weight: 500; }
</style>

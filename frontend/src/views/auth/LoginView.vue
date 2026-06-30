<template>
  <div class="login-page">
    <div class="login-card card">
      <div class="login-header">
        <div class="login-logo">HR</div>
        <h1 class="login-title">HR Portal</h1>
        <p class="login-subtitle">계정에 로그인하세요</p>
      </div>

      <form @submit.prevent="handleLogin" novalidate>
        <div class="form-group">
          <label for="username">아이디</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            placeholder="아이디를 입력하세요"
            :disabled="isLoading"
            autocomplete="username"
            :class="{ 'input-error': errors.username && touched.username }"
            @blur="touched.username = true; validateField('username')"
            @input="validateField('username')"
          />
          <p v-if="errors.username && touched.username" class="field-error">{{ errors.username }}</p>
        </div>

        <div class="form-group">
          <label for="password">비밀번호</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="비밀번호를 입력하세요"
            :disabled="isLoading"
            autocomplete="current-password"
            :class="{ 'input-error': errors.password && touched.password }"
            @blur="touched.password = true; validateField('password')"
            @input="validateField('password')"
          />
          <p v-if="errors.password && touched.password" class="field-error">{{ errors.password }}</p>
        </div>

        <button type="submit" class="btn btn-primary login-btn" :disabled="isLoading">
          <span v-if="isLoading" class="spinner" style="width:16px;height:16px;margin-right:8px;"></span>
          {{ isLoading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <div class="forgot-link">
        <button class="link-btn" @click="openForgotModal">비밀번호를 잊으셨나요?</button>
      </div>
    </div>


    <!-- 알림 모달 -->
    <Teleport to="body">
      <div v-if="alertModal.visible" class="modal-overlay">
        <div class="modal alert-modal" role="alertdialog" aria-modal="true">
          <button class="modal-close-btn" @click="closeAlertModal" aria-label="닫기">✕</button>
          <div class="modal-icon" :class="alertModal.type">
            {{ alertModal.type === 'warning' ? '!' : '✕' }}
          </div>
          <h2 class="modal-title">{{ alertModal.title }}</h2>
          <p class="modal-message">{{ alertModal.message }}</p>
          <button class="btn btn-primary modal-confirm-btn" @click="closeAlertModal">닫기</button>
        </div>
      </div>
    </Teleport>

    <!-- 비밀번호 찾기 모달 -->
    <Teleport to="body">
      <div v-if="forgotModal.visible" class="modal-overlay" @click.self="closeForgotModal">
        <div class="modal forgot-modal" role="dialog" aria-modal="true">

          <!-- 초기 입력 단계 -->
          <template v-if="!forgotModal.result">
            <h2 class="modal-title">비밀번호 찾기</h2>
            <p class="modal-desc">아이디와 생년월일로 본인 인증 후<br>임시 비밀번호를 발급해드립니다.</p>

            <div v-if="forgotModal.errorMessage" class="alert alert-error" style="margin-bottom:16px;">
              {{ forgotModal.errorMessage }}
            </div>

            <div class="form-group">
              <label>아이디</label>
              <input v-model="forgotModal.username" type="text" placeholder="아이디 입력" :disabled="forgotModal.isLoading" />
            </div>
            <div class="form-group">
              <label>생년월일</label>
              <input v-model="forgotModal.dateOfBirth" type="date" :disabled="forgotModal.isLoading" />
            </div>

            <div class="modal-actions">
              <button class="btn btn-secondary" @click="closeForgotModal" :disabled="forgotModal.isLoading">취소</button>
              <button class="btn btn-primary" @click="handleForgotPassword" :disabled="forgotModal.isLoading">
                <span v-if="forgotModal.isLoading" class="spinner" style="width:14px;height:14px;margin-right:6px;"></span>
                {{ forgotModal.isLoading ? '처리 중...' : '임시 비밀번호 발급' }}
              </button>
            </div>
          </template>

          <!-- 결과 단계 -->
          <template v-else>
            <div class="result-icon">✓</div>
            <h2 class="modal-title">임시 비밀번호 발급 완료</h2>

            <div class="temp-pw-box">
              <span class="temp-pw-value">{{ forgotModal.result.temporaryPassword }}</span>
              <button class="btn btn-outline copy-btn" @click="copyTempPassword">
                {{ copied ? '복사됨!' : '복사' }}
              </button>
            </div>

            <div class="warning-box">
              <p>⚠️ 보안 경고</p>
              <p>이 임시 비밀번호는 화면을 닫으면 다시 확인할 수 없습니다.<br>
              지금 바로 복사하고, 로그인 후 반드시 비밀번호를 변경해주세요.</p>
            </div>

            <button class="btn btn-primary" style="width:100%;margin-top:16px;" @click="closeForgotModal">확인</button>
          </template>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth.js'
import { login, forgotPassword } from '../../api/auth.js'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({ username: '', password: '' })
const isLoading = ref(false)

const errors = reactive({ username: '', password: '' })
const touched = reactive({ username: false, password: false })

const alertModal = reactive({ visible: false, type: 'warning', title: '', message: '' })

function showAlert(title, message, type = 'warning') {
  alertModal.title = title
  alertModal.message = message
  alertModal.type = type
  alertModal.visible = true
}

function closeAlertModal() {
  alertModal.visible = false
}

const PASSWORD_REGEX = /^(?=.*[a-zA-Z])(?=.*\d).{8,}$/

function validateField(field) {
  if (field === 'username') {
    errors.username = form.username ? '' : '아이디를 입력해주세요.'
  }
  if (field === 'password') {
    if (!form.password) {
      errors.password = '비밀번호를 입력해주세요.'
    } else if (!PASSWORD_REGEX.test(form.password)) {
      errors.password = '비밀번호는 8자 이상, 영문+숫자를 포함해야 합니다.'
    } else {
      errors.password = ''
    }
  }
}

function validateAll() {
  touched.username = true
  touched.password = true
  validateField('username')
  validateField('password')
  return !errors.username && !errors.password
}

const forgotModal = reactive({
  visible: false,
  username: '',
  dateOfBirth: '',
  isLoading: false,
  errorMessage: '',
  result: null
})
const copied = ref(false)

async function handleLogin() {
  // 미입력 검증
  const emptyUsername = !form.username.trim()
  const emptyPassword = !form.password.trim()

  if (emptyUsername && emptyPassword) {
    showAlert('입력 오류', '아이디와 비밀번호를 입력해주세요.')
    return
  }
  if (emptyUsername) {
    showAlert('입력 오류', '아이디를 입력해주세요.')
    return
  }
  if (emptyPassword) {
    showAlert('입력 오류', '비밀번호를 입력해주세요.')
    return
  }

  if (!validateAll()) return

  isLoading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    const data = res.data.data
    authStore.setAuth(data)
    if (data.role === 'ROLE_ADMIN') {
      router.push('/admin/employees')
    } else {
      router.push('/profile')
    }
  } catch (err) {
    form.username = ''
    form.password = ''
    touched.username = false
    touched.password = false
    errors.username = ''
    errors.password = ''
    showAlert('로그인 실패', err?.message || '아이디 또는 비밀번호가 올바르지 않습니다.', 'error')
  } finally {
    isLoading.value = false
  }
}

function openForgotModal() {
  forgotModal.visible = true
  forgotModal.username = ''
  forgotModal.dateOfBirth = ''
  forgotModal.errorMessage = ''
  forgotModal.result = null
  copied.value = false
}

function closeForgotModal() {
  forgotModal.visible = false
}

async function handleForgotPassword() {
  forgotModal.errorMessage = ''
  if (!forgotModal.username || !forgotModal.dateOfBirth) {
    forgotModal.errorMessage = '아이디와 생년월일을 모두 입력해주세요.'
    return
  }
  forgotModal.isLoading = true
  try {
    const res = await forgotPassword({
      username: forgotModal.username,
      dateOfBirth: forgotModal.dateOfBirth
    })
    forgotModal.result = res.data.data
  } catch (err) {
    forgotModal.errorMessage = err?.message || '인증에 실패했습니다.'
  } finally {
    forgotModal.isLoading = false
  }
}

async function copyTempPassword() {
  try {
    await navigator.clipboard.writeText(forgotModal.result.temporaryPassword)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    copied.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #EEF2FF 0%, #F0FDF4 100%);
  padding: 24px;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  width: 56px;
  height: 56px;
  background: #4F46E5;
  color: white;
  font-size: 20px;
  font-weight: 700;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.login-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 4px;
}

.login-subtitle {
  font-size: 14px;
  color: #6B7280;
}

.login-btn {
  width: 100%;
  padding: 12px;
  font-size: 15px;
  margin-top: 8px;
}

.forgot-link {
  text-align: center;
  margin-top: 16px;
}

.modal-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  margin: 0 auto 16px;
}

.modal-icon.warning {
  background: #FEF3C7;
  color: #92400E;
}

.modal-icon.error {
  background: #FEE2E2;
  color: #991B1B;
}

.alert-modal {
  max-width: 360px;
  text-align: center;
  position: relative;
}

.modal-close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  background: none;
  border: none;
  font-size: 16px;
  color: #9CA3AF;
  cursor: pointer;
  padding: 4px;
  line-height: 1;
}

.modal-close-btn:hover {
  color: #374151;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 8px;
}

.modal-message {
  text-align: center;
  color: #6B7280;
  font-size: 14px;
  margin-bottom: 24px;
}

.modal-confirm-btn {
  width: 100%;
}

.forgot-modal {
  max-width: 440px;
}

.modal-desc {
  text-align: center;
  color: #6B7280;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 24px;
}

.modal-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 8px;
}

.result-icon {
  width: 52px;
  height: 52px;
  background: #D1FAE5;
  color: #065F46;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin: 0 auto 16px;
}

.temp-pw-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #F3F4F6;
  border-radius: 8px;
  padding: 12px 16px;
  margin: 16px 0;
}

.temp-pw-value {
  flex: 1;
  font-family: monospace;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
  color: #111827;
}

.copy-btn {
  flex-shrink: 0;
  padding: 6px 12px;
  font-size: 13px;
}

.warning-box {
  background: #FFF7ED;
  border: 1px solid #FED7AA;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 13px;
  color: #92400E;
  line-height: 1.6;
}

.warning-box p:first-child {
  font-weight: 600;
  margin-bottom: 4px;
}

.input-error {
  border-color: #EF4444 !important;
  background-color: #FFF5F5;
}

.input-error:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15) !important;
}

.field-error {
  margin-top: 4px;
  font-size: 12px;
  color: #EF4444;
}
</style>

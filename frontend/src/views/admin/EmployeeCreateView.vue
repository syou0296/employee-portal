<template>
  <div>
    <div class="page-header">
      <div style="display:flex;align-items:center;gap:12px;">
        <button class="btn btn-secondary" @click="router.back()" style="padding:6px 12px;">← 뒤로</button>
        <h1 class="page-title">직원 추가</h1>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <div class="card" style="max-width:760px;">
      <form @submit.prevent="handleSubmit">
        <h2 class="section-title">계정 정보</h2>
        <div class="form-row">
          <div class="form-group">
            <label>사용자명 *</label>
            <input v-model="form.username" type="text" :class="{ error: fieldErrors.username }" required />
            <span v-if="fieldErrors.username" class="field-error">{{ fieldErrors.username }}</span>
          </div>
          <div class="form-group">
            <label>비밀번호 *</label>
            <input v-model="form.password" type="password" :class="{ error: fieldErrors.password }" required />
            <span v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</span>
          </div>
          <div class="form-group">
            <label>권한 *</label>
            <select v-model="form.role" :class="{ error: fieldErrors.role }">
              <option value="ROLE_USER">일반 사용자</option>
              <option value="ROLE_ADMIN">관리자</option>
            </select>
            <span v-if="fieldErrors.role" class="field-error">{{ fieldErrors.role }}</span>
          </div>
        </div>

        <hr class="divider" />
        <h2 class="section-title">개인 정보</h2>
        <div class="form-row">
          <div class="form-group">
            <label>성 (Last Name) *</label>
            <input v-model="form.lastName" type="text" :class="{ error: fieldErrors.lastName }" required />
            <span v-if="fieldErrors.lastName" class="field-error">{{ fieldErrors.lastName }}</span>
          </div>
          <div class="form-group">
            <label>이름 (First Name) *</label>
            <input v-model="form.firstName" type="text" :class="{ error: fieldErrors.firstName }" required />
            <span v-if="fieldErrors.firstName" class="field-error">{{ fieldErrors.firstName }}</span>
          </div>
          <div class="form-group">
            <label>생년월일 *</label>
            <input v-model="form.dateOfBirth" type="date" :class="{ error: fieldErrors.dateOfBirth }" required />
            <span v-if="fieldErrors.dateOfBirth" class="field-error">{{ fieldErrors.dateOfBirth }}</span>
          </div>
        </div>

        <hr class="divider" />
        <h2 class="section-title">연락처 및 직무</h2>
        <div class="form-row">
          <div class="form-group">
            <label>이메일 *</label>
            <input v-model="form.email" type="email" :class="{ error: fieldErrors.email }" required />
            <span v-if="fieldErrors.email" class="field-error">{{ fieldErrors.email }}</span>
          </div>
          <div class="form-group">
            <label>전화번호</label>
            <input v-model="form.phone" type="text" :class="{ error: fieldErrors.phone }" />
            <span v-if="fieldErrors.phone" class="field-error">{{ fieldErrors.phone }}</span>
          </div>
          <div class="form-group">
            <label>부서</label>
            <select v-model="form.department" :class="{ error: fieldErrors.department }">
              <option value="">선택 안 함</option>
              <option value="개발팀">Engineering</option>
              <option value="인사팀">Human Resources</option>
              <option value="영업팀">Sales</option>
            </select>
            <span v-if="fieldErrors.department" class="field-error">{{ fieldErrors.department }}</span>
          </div>
          <div class="form-group">
            <label>직급/포지션</label>
            <select v-model="form.position" :class="{ error: fieldErrors.position }">
              <option value="">선택 안 함</option>
              <option value="사원">Staff</option>
              <option value="대리">Assistant</option>
              <option value="과장">Manager</option>
            </select>
            <span v-if="fieldErrors.position" class="field-error">{{ fieldErrors.position }}</span>
          </div>
          <div class="form-group">
            <label>입사일 *</label>
            <input v-model="form.hireDate" type="date" :class="{ error: fieldErrors.hireDate }" required />
            <span v-if="fieldErrors.hireDate" class="field-error">{{ fieldErrors.hireDate }}</span>
          </div>
        </div>

        <div style="display:flex;gap:12px;margin-top:24px;">
          <button type="button" class="btn btn-secondary" @click="router.back()" :disabled="isLoading">취소</button>
          <button type="submit" class="btn btn-primary" :disabled="isLoading">
            <span v-if="isLoading" class="spinner" style="width:14px;height:14px;margin-right:6px;"></span>
            {{ isLoading ? '저장 중...' : '직원 등록' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createEmployee } from '../../api/admin.js'

const router = useRouter()
const isLoading = ref(false)
const errorMessage = ref('')
const fieldErrors = reactive({})

const form = reactive({
  username: '',
  password: '',
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  department: '',
  position: '',
  dateOfBirth: '',
  hireDate: '',
  role: 'ROLE_USER'
})

async function handleSubmit() {
  isLoading.value = true
  errorMessage.value = ''
  Object.keys(fieldErrors).forEach(k => delete fieldErrors[k])
  try {
    const res = await createEmployee({ ...form })
    const newEmp = res.data.data
    router.push('/admin/employees/' + newEmp.employeeId)
  } catch (err) {
    if (err?.fieldErrors) {
      err.fieldErrors.forEach(fe => { fieldErrors[fe.field] = fe.message })
    } else {
      errorMessage.value = err?.message || '직원 등록에 실패했습니다.'
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.section-title { font-size: 15px; font-weight: 600; color: #374151; margin-bottom: 16px; }
.form-row { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 0 24px; }
.divider { border: none; border-top: 1px solid #F3F4F6; margin: 20px 0; }
</style>

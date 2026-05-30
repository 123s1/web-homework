<template>
  <div class="app-shell">
    <main class="main-content d-flex align-items-center py-5">
      <div class="container">
        <div class="row justify-content-center">
          <div class="col-lg-7 col-xl-6">
            <div class="content-card p-4 p-lg-5">
              <span class="status-pill mb-3">管理员后台</span>
              <h1 class="section-title mb-3">管理员登录</h1>
              <p class="text-muted-strong mb-4">请使用后台管理员账号登录，进入预约数据统计和业务管理页面。</p>

              <div v-if="currentAdmin" class="alert alert-warning rounded-4 mb-4">
                当前已保存管理员信息：{{ currentAdmin.username }}，角色 {{ currentAdmin.role }}
              </div>

              <ErrorAlert :message="errorMessage" />

              <form class="row g-4" @submit.prevent="submitLogin">
                <div class="col-12">
                  <label class="form-label fw-semibold" for="username">用户名</label>
                  <input
                    id="username"
                    v-model.trim="form.username"
                    class="form-control form-control-lg rounded-4"
                    :class="{ 'is-invalid': validationErrors.username }"
                    type="text"
                    placeholder="请输入管理员用户名"
                    autocomplete="username"
                  />
                  <div class="invalid-feedback">{{ validationErrors.username }}</div>
                </div>

                <div class="col-12">
                  <label class="form-label fw-semibold" for="password">密码</label>
                  <input
                    id="password"
                    v-model="form.password"
                    class="form-control form-control-lg rounded-4"
                    :class="{ 'is-invalid': validationErrors.password }"
                    type="password"
                    placeholder="请输入管理员密码"
                    autocomplete="current-password"
                  />
                  <div class="invalid-feedback">{{ validationErrors.password }}</div>
                </div>

                <div v-if="successMessage" class="col-12">
                  <div class="alert alert-success rounded-4 mb-0">{{ successMessage }}</div>
                </div>

                <div class="col-12 d-flex flex-wrap gap-3">
                  <button class="btn btn-museum" type="submit" :disabled="submitting">
                    <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
                    {{ submitting ? '登录中...' : '登录后台' }}
                  </button>
                  <button class="btn btn-outline-museum" type="button" @click="resetForm">重新填写</button>
                  <RouterLink class="btn btn-link text-decoration-none" to="/">返回游客首页</RouterLink>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import ErrorAlert from '../../components/ErrorAlert.vue'
import { loginAdmin } from '../../api/admin'
import { getAdminInfo, setAdminInfo } from '../../utils/storage'

const router = useRouter()
const currentAdmin = ref(getAdminInfo())
const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const form = reactive({
  username: currentAdmin.value?.username || '',
  password: ''
})

const validationErrors = reactive({
  username: '',
  password: ''
})

async function submitLogin() {
  errorMessage.value = ''
  successMessage.value = ''
  if (!validateForm()) {
    return
  }

  submitting.value = true
  try {
    const admin = await loginAdmin({
      username: form.username,
      password: form.password
    })
    setAdminInfo(admin)
    currentAdmin.value = admin
    successMessage.value = '登录成功，即将进入后台首页'
    setTimeout(() => {
      router.push('/admin')
    }, 500)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    submitting.value = false
  }
}

function validateForm() {
  validationErrors.username = ''
  validationErrors.password = ''

  if (!form.username) {
    validationErrors.username = '用户名不能为空'
  }
  if (!form.password) {
    validationErrors.password = '密码不能为空'
  }

  return !validationErrors.username && !validationErrors.password
}

function resetForm() {
  form.username = ''
  form.password = ''
  validationErrors.username = ''
  validationErrors.password = ''
  errorMessage.value = ''
  successMessage.value = ''
}
</script>

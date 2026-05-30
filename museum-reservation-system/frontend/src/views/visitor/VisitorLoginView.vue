<template>
  <VisitorLayout>
    <section class="placeholder-page">
      <div class="container">
        <div class="row justify-content-center">
          <div class="col-lg-8 col-xl-7">
            <div class="content-card p-4 p-lg-5">
              <span class="status-pill mb-3">游客实名登录</span>
              <h1 class="section-title mb-3">填写实名信息后开始预约</h1>
              <p class="text-muted-strong mb-4">系统将使用实名信息创建或更新游客档案，后续预约会自动带入姓名、身份证号和手机号。</p>

              <div v-if="currentVisitor" class="alert alert-warning rounded-4 mb-4">
                当前已保存游客信息：{{ currentVisitor.name }}，身份证号 {{ currentVisitor.idCard }}
              </div>

              <ErrorAlert :message="errorMessage" />

              <form class="row g-4" @submit.prevent="submitLogin">
                <div class="col-12">
                  <label class="form-label fw-semibold" for="visitorName">姓名</label>
                  <input
                    id="visitorName"
                    v-model.trim="form.name"
                    class="form-control form-control-lg rounded-4"
                    :class="{ 'is-invalid': validationErrors.name }"
                    type="text"
                    placeholder="请输入真实姓名"
                    autocomplete="name"
                  />
                  <div class="invalid-feedback">{{ validationErrors.name }}</div>
                </div>

                <div class="col-12">
                  <label class="form-label fw-semibold" for="visitorIdCard">身份证号</label>
                  <input
                    id="visitorIdCard"
                    v-model.trim="form.idCard"
                    class="form-control form-control-lg rounded-4"
                    :class="{ 'is-invalid': validationErrors.idCard }"
                    type="text"
                    placeholder="请输入 18 位身份证号"
                    maxlength="18"
                    autocomplete="off"
                  />
                  <div class="invalid-feedback">{{ validationErrors.idCard }}</div>
                </div>

                <div class="col-12">
                  <label class="form-label fw-semibold" for="visitorPhone">手机号</label>
                  <input
                    id="visitorPhone"
                    v-model.trim="form.phone"
                    class="form-control form-control-lg rounded-4"
                    :class="{ 'is-invalid': validationErrors.phone }"
                    type="tel"
                    placeholder="请输入 11 位手机号"
                    maxlength="11"
                    autocomplete="tel"
                  />
                  <div class="invalid-feedback">{{ validationErrors.phone }}</div>
                </div>

                <div v-if="successMessage" class="col-12">
                  <div class="alert alert-success rounded-4 mb-0">{{ successMessage }}</div>
                </div>

                <div class="col-12 d-flex flex-wrap gap-3">
                  <button class="btn btn-museum" type="submit" :disabled="submitting">
                    <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
                    {{ submitting ? '登录中...' : '确认登录并预约' }}
                  </button>
                  <button class="btn btn-outline-museum" type="button" @click="resetForm">重新填写</button>
                  <RouterLink class="btn btn-link text-decoration-none" to="/">返回首页</RouterLink>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </section>
  </VisitorLayout>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import ErrorAlert from '../../components/ErrorAlert.vue'
import VisitorLayout from '../../layouts/VisitorLayout.vue'
import { loginVisitor } from '../../api/visitor'
import { getVisitorInfo, setVisitorInfo } from '../../utils/storage'
import { isValidIdCard, isValidPhone } from '../../utils/validators'

const router = useRouter()
const currentVisitor = ref(getVisitorInfo())
const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const form = reactive({
  name: currentVisitor.value?.name || '',
  idCard: currentVisitor.value?.idCard || '',
  phone: currentVisitor.value?.phone || ''
})

const validationErrors = reactive({
  name: '',
  idCard: '',
  phone: ''
})

async function submitLogin() {
  errorMessage.value = ''
  successMessage.value = ''
  if (!validateForm()) {
    return
  }

  submitting.value = true
  try {
    const visitor = await loginVisitor({
      name: form.name,
      idCard: form.idCard.toUpperCase(),
      phone: form.phone
    })
    setVisitorInfo(visitor)
    currentVisitor.value = visitor
    successMessage.value = '登录成功，即将进入预约时段页面'
    setTimeout(() => {
      router.push('/slots')
    }, 500)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    submitting.value = false
  }
}

function validateForm() {
  validationErrors.name = ''
  validationErrors.idCard = ''
  validationErrors.phone = ''

  if (!form.name) {
    validationErrors.name = '姓名不能为空'
  }
  if (!isValidIdCard(form.idCard)) {
    validationErrors.idCard = '身份证号格式不正确'
  }
  if (!isValidPhone(form.phone)) {
    validationErrors.phone = '手机号格式不正确'
  }

  return !validationErrors.name && !validationErrors.idCard && !validationErrors.phone
}

function resetForm() {
  form.name = ''
  form.idCard = ''
  form.phone = ''
  validationErrors.name = ''
  validationErrors.idCard = ''
  validationErrors.phone = ''
  errorMessage.value = ''
  successMessage.value = ''
}
</script>

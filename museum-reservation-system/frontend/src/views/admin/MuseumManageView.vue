<template>
  <AdminLayout>
    <div class="content-card p-4 p-lg-5 mb-4">
      <div class="row align-items-end g-4">
        <div class="col-lg-8">
          <span class="status-pill mb-3">场馆管理</span>
          <h1 class="section-title mb-3">场馆信息管理</h1>
          <p class="text-muted-strong mb-0">维护游客首页展示的场馆名称、地址、开放说明、参观须知和开放状态。</p>
        </div>
        <div class="col-lg-4 text-lg-end">
          <button class="btn btn-outline-museum" type="button" :disabled="loading" @click="loadMuseumInfo">刷新信息</button>
        </div>
      </div>
    </div>

    <ErrorAlert :message="errorMessage" />
    <LoadingState v-if="loading" />

    <div v-else class="row g-4">
      <div class="col-xl-7">
        <div class="content-card p-4 p-lg-5">
          <h2 class="h4 fw-bold mb-4">编辑场馆资料</h2>

          <form class="row g-4" @submit.prevent="submitForm">
            <div class="col-12">
              <label class="form-label fw-semibold" for="name">场馆名称</label>
              <input
                id="name"
                v-model.trim="form.name"
                class="form-control rounded-4"
                :class="{ 'is-invalid': validationErrors.name }"
                type="text"
                placeholder="请输入场馆名称"
              />
              <div class="invalid-feedback">{{ validationErrors.name }}</div>
            </div>

            <div class="col-12">
              <label class="form-label fw-semibold" for="address">场馆地址</label>
              <input
                id="address"
                v-model.trim="form.address"
                class="form-control rounded-4"
                :class="{ 'is-invalid': validationErrors.address }"
                type="text"
                placeholder="请输入场馆地址"
              />
              <div class="invalid-feedback">{{ validationErrors.address }}</div>
            </div>

            <div class="col-12">
              <label class="form-label fw-semibold" for="openInfo">开放时间说明</label>
              <textarea
                id="openInfo"
                v-model.trim="form.openInfo"
                class="form-control rounded-4"
                :class="{ 'is-invalid': validationErrors.openInfo }"
                rows="4"
                placeholder="请输入开放时间、闭馆日期等说明"
              ></textarea>
              <div class="invalid-feedback">{{ validationErrors.openInfo }}</div>
            </div>

            <div class="col-12">
              <label class="form-label fw-semibold" for="rules">参观须知</label>
              <textarea
                id="rules"
                v-model.trim="form.rules"
                class="form-control rounded-4"
                :class="{ 'is-invalid': validationErrors.rules }"
                rows="5"
                placeholder="请输入实名预约、入馆证件、参观纪律等规则"
              ></textarea>
              <div class="invalid-feedback">{{ validationErrors.rules }}</div>
            </div>

            <div class="col-12">
              <label class="form-label fw-semibold" for="status">场馆状态</label>
              <select id="status" v-model.number="form.status" class="form-select rounded-4">
                <option :value="1">正常开放</option>
                <option :value="0">暂停开放</option>
              </select>
            </div>

            <div v-if="successMessage" class="col-12">
              <div class="alert alert-success rounded-4 mb-0">{{ successMessage }}</div>
            </div>

            <div class="col-12 d-flex flex-wrap gap-3">
              <button class="btn btn-museum" type="submit" :disabled="saving">
                <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
                {{ saving ? '保存中...' : '保存修改' }}
              </button>
              <button class="btn btn-outline-museum" type="button" :disabled="saving" @click="resetForm">恢复当前数据</button>
            </div>
          </form>
        </div>
      </div>

      <div class="col-xl-5">
        <div class="content-card p-4 p-lg-5 h-100">
          <div class="d-flex justify-content-between align-items-start gap-3 mb-4">
            <div>
              <h2 class="h4 fw-bold mb-1">游客端预览</h2>
              <div class="text-muted">保存后游客首页将展示这些信息。</div>
            </div>
            <span class="badge rounded-pill" :class="form.status === 1 ? 'text-bg-success' : 'text-bg-secondary'">
              {{ form.status === 1 ? '正常开放' : '暂停开放' }}
            </span>
          </div>

          <div class="border rounded-4 p-4 bg-light mb-4">
            <div class="small text-muted mb-1">场馆名称</div>
            <div class="fs-4 fw-bold">{{ form.name || '-' }}</div>
          </div>

          <div class="mb-4">
            <div class="small text-muted mb-1">地址</div>
            <div class="fw-semibold">{{ form.address || '-' }}</div>
          </div>

          <div class="mb-4">
            <div class="small text-muted mb-1">开放说明</div>
            <div class="text-pre-line">{{ form.openInfo || '-' }}</div>
          </div>

          <div class="mb-4">
            <div class="small text-muted mb-1">参观须知</div>
            <div class="text-pre-line">{{ form.rules || '-' }}</div>
          </div>

          <div class="pt-3 border-top">
            <div class="small text-muted mb-1">最后更新时间</div>
            <div class="fw-semibold">{{ formatDateTime(originalInfo?.updatedAt) }}</div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getAdminMuseumInfo, updateAdminMuseumInfo } from '../../api/admin'
import { formatDateTime } from '../../utils/format'

const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const originalInfo = ref(null)

const form = reactive({
  name: '',
  address: '',
  openInfo: '',
  rules: '',
  status: 1
})

const validationErrors = reactive({
  name: '',
  address: '',
  openInfo: '',
  rules: ''
})

onMounted(() => {
  loadMuseumInfo()
})

async function loadMuseumInfo() {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const info = await getAdminMuseumInfo()
    originalInfo.value = info
    fillForm(info)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

async function submitForm() {
  errorMessage.value = ''
  successMessage.value = ''
  if (!validateForm()) {
    return
  }

  saving.value = true
  try {
    await updateAdminMuseumInfo({
      name: form.name,
      address: form.address,
      openInfo: form.openInfo,
      rules: form.rules,
      status: form.status
    })
    successMessage.value = '场馆信息保存成功'
    await loadMuseumInfo()
    successMessage.value = '场馆信息保存成功'
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    saving.value = false
  }
}

function resetForm() {
  if (originalInfo.value) {
    fillForm(originalInfo.value)
  }
  clearValidation()
  errorMessage.value = ''
  successMessage.value = ''
}

function fillForm(info) {
  form.name = info?.name || ''
  form.address = info?.address || ''
  form.openInfo = info?.openInfo || ''
  form.rules = info?.rules || ''
  form.status = info?.status ?? 1
}

function validateForm() {
  clearValidation()

  if (!form.name) {
    validationErrors.name = '场馆名称不能为空'
  }
  if (!form.address) {
    validationErrors.address = '场馆地址不能为空'
  }
  if (!form.openInfo) {
    validationErrors.openInfo = '开放时间说明不能为空'
  }
  if (!form.rules) {
    validationErrors.rules = '参观须知不能为空'
  }

  return !validationErrors.name && !validationErrors.address && !validationErrors.openInfo && !validationErrors.rules
}

function clearValidation() {
  validationErrors.name = ''
  validationErrors.address = ''
  validationErrors.openInfo = ''
  validationErrors.rules = ''
}
</script>

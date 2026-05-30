<template>
  <AdminLayout>
    <div class="content-card p-4 p-lg-5 mb-4">
      <div class="row align-items-end g-4">
        <div class="col-xl-5">
          <span class="status-pill mb-3">预约活动</span>
          <h1 class="section-title mb-3">预约活动管理</h1>
          <p class="text-muted-strong mb-0">维护参观日期、预约开放时间、容量上限和活动状态。</p>
        </div>
        <div class="col-xl-7">
          <form class="row g-3" @submit.prevent="loadActivities">
            <div class="col-md-4">
              <label class="form-label fw-semibold" for="filterStatus">活动状态</label>
              <select id="filterStatus" v-model="filters.status" class="form-select rounded-4">
                <option value="">全部状态</option>
                <option v-for="option in statusOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label fw-semibold" for="startDate">开始日期</label>
              <input id="startDate" v-model="filters.startDate" class="form-control rounded-4" type="date" />
            </div>
            <div class="col-md-3">
              <label class="form-label fw-semibold" for="endDate">结束日期</label>
              <input id="endDate" v-model="filters.endDate" class="form-control rounded-4" type="date" />
            </div>
            <div class="col-md-2 d-grid align-self-end">
              <button class="btn btn-museum" type="submit" :disabled="loading">查询</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <ErrorAlert :message="errorMessage" />
    <div v-if="successMessage" class="alert alert-success rounded-4">{{ successMessage }}</div>

    <div class="row g-4">
      <div class="col-xl-5">
        <div class="content-card p-4 p-lg-5">
          <div class="d-flex justify-content-between align-items-center gap-3 mb-4">
            <div>
              <h2 class="h4 fw-bold mb-1">{{ editingId ? '编辑活动' : '新增活动' }}</h2>
              <div class="text-muted">创建或调整每日预约活动的开放规则。</div>
            </div>
            <button v-if="editingId" class="btn btn-sm btn-outline-secondary rounded-pill" type="button" @click="resetForm">取消编辑</button>
          </div>

          <form class="row g-4" @submit.prevent="submitForm">
            <div class="col-12">
              <label class="form-label fw-semibold" for="activityName">活动名称</label>
              <input id="activityName" v-model.trim="form.activityName" class="form-control rounded-4" :class="{ 'is-invalid': validationErrors.activityName }" type="text" placeholder="例如：常设展预约" />
              <div class="invalid-feedback">{{ validationErrors.activityName }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="visitDate">参观日期</label>
              <input id="visitDate" v-model="form.visitDate" class="form-control rounded-4" :class="{ 'is-invalid': validationErrors.visitDate }" type="date" />
              <div class="invalid-feedback">{{ validationErrors.visitDate }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="status">活动状态</label>
              <select id="status" v-model="form.status" class="form-select rounded-4" :class="{ 'is-invalid': validationErrors.status }">
                <option v-for="option in statusOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
              </select>
              <div class="invalid-feedback">{{ validationErrors.status }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="dailyCapacity">每日总名额</label>
              <input id="dailyCapacity" v-model.number="form.dailyCapacity" class="form-control rounded-4" :class="{ 'is-invalid': validationErrors.dailyCapacity }" type="number" min="1" />
              <div class="invalid-feedback">{{ validationErrors.dailyCapacity }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="personLimit">单人预约上限</label>
              <input id="personLimit" v-model.number="form.personLimit" class="form-control rounded-4" :class="{ 'is-invalid': validationErrors.personLimit }" type="number" min="1" />
              <div class="invalid-feedback">{{ validationErrors.personLimit }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="bookingStart">预约开始时间</label>
              <input id="bookingStart" v-model="form.bookingStart" class="form-control rounded-4" :class="{ 'is-invalid': validationErrors.bookingStart }" type="datetime-local" />
              <div class="invalid-feedback">{{ validationErrors.bookingStart }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="bookingEnd">预约结束时间</label>
              <input id="bookingEnd" v-model="form.bookingEnd" class="form-control rounded-4" :class="{ 'is-invalid': validationErrors.bookingEnd }" type="datetime-local" />
              <div class="invalid-feedback">{{ validationErrors.bookingEnd }}</div>
            </div>

            <div class="col-12 d-flex flex-wrap gap-3">
              <button class="btn btn-museum" type="submit" :disabled="saving">
                <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
                {{ saving ? '保存中...' : editingId ? '保存修改' : '新增活动' }}
              </button>
              <button class="btn btn-outline-museum" type="button" :disabled="saving" @click="resetForm">清空表单</button>
            </div>
          </form>
        </div>
      </div>

      <div class="col-xl-7">
        <LoadingState v-if="loading" />

        <div v-else-if="activities.length" class="d-flex flex-column gap-3">
          <article v-for="activity in activities" :key="activity.id" class="content-card p-4">
            <div class="d-flex flex-column flex-lg-row justify-content-between gap-3">
              <div class="flex-grow-1">
                <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                  <span class="badge rounded-pill" :class="getStatusBadgeClass(activity.status)">
                    {{ getStatusLabel(activity.status) }}
                  </span>
                  <span class="badge rounded-pill text-bg-light">{{ formatDate(activity.visitDate) }}</span>
                </div>
                <h3 class="h5 fw-bold mb-3">{{ activity.activityName }}</h3>
                <div class="row g-3 mb-3">
                  <div class="col-md-4">
                    <div class="small text-muted">每日总名额</div>
                    <div class="fw-bold">{{ activity.dailyCapacity }}</div>
                  </div>
                  <div class="col-md-4">
                    <div class="small text-muted">单人上限</div>
                    <div class="fw-bold">{{ activity.personLimit }}</div>
                  </div>
                  <div class="col-md-4">
                    <div class="small text-muted">活动编号</div>
                    <div class="fw-bold">{{ activity.id }}</div>
                  </div>
                </div>
                <div class="small text-muted mb-2">
                  预约开放：{{ formatDateTime(activity.bookingStart) }} 至 {{ formatDateTime(activity.bookingEnd) }}
                </div>
                <div class="small text-muted">
                  创建：{{ formatDateTime(activity.createdAt) }} / 更新：{{ formatDateTime(activity.updatedAt) }}
                </div>
              </div>
              <div class="d-flex flex-lg-column gap-2 align-self-start">
                <button class="btn btn-sm btn-outline-museum" type="button" @click="startEdit(activity)">编辑</button>
                <button class="btn btn-sm btn-outline-secondary" type="button" @click="toggleOpen(activity)">
                  {{ activity.status === 'OPEN' ? '关闭' : '开放' }}
                </button>
              </div>
            </div>
          </article>
        </div>

        <EmptyState v-else text="暂无预约活动数据，请新增活动或调整筛选条件" />
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { createAdminActivity, listAdminActivities, updateAdminActivity } from '../../api/admin'
import { formatDate, formatDateTime } from '../../utils/format'
import { isPositiveNumber } from '../../utils/validators'

const statusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'OPEN', label: '开放预约' },
  { value: 'CLOSED', label: '已关闭' },
  { value: 'DISABLED', label: '已禁用' }
]

const filters = reactive({
  status: '',
  startDate: '',
  endDate: ''
})

const form = reactive({
  activityName: '',
  visitDate: '',
  dailyCapacity: 100,
  personLimit: 1,
  bookingStart: '',
  bookingEnd: '',
  status: 'DRAFT'
})

const validationErrors = reactive({
  activityName: '',
  visitDate: '',
  dailyCapacity: '',
  personLimit: '',
  bookingStart: '',
  bookingEnd: '',
  status: ''
})

const activities = ref([])
const editingId = ref(null)
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

onMounted(() => {
  loadActivities()
})

async function loadActivities() {
  loading.value = true
  errorMessage.value = ''
  try {
    const params = {}
    if (filters.status) {
      params.status = filters.status
    }
    if (filters.startDate) {
      params.startDate = filters.startDate
    }
    if (filters.endDate) {
      params.endDate = filters.endDate
    }
    activities.value = await listAdminActivities(params)
  } catch (error) {
    errorMessage.value = error.message
    activities.value = []
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
    const data = buildRequestData()
    if (editingId.value) {
      await updateAdminActivity(editingId.value, data)
      successMessage.value = '预约活动修改成功'
    } else {
      await createAdminActivity(data)
      successMessage.value = '预约活动新增成功'
    }
    resetForm()
    await loadActivities()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    saving.value = false
  }
}

function startEdit(activity) {
  editingId.value = activity.id
  form.activityName = activity.activityName || ''
  form.visitDate = activity.visitDate || ''
  form.dailyCapacity = activity.dailyCapacity || 100
  form.personLimit = activity.personLimit || 1
  form.bookingStart = toDateTimeInput(activity.bookingStart)
  form.bookingEnd = toDateTimeInput(activity.bookingEnd)
  form.status = activity.status || 'DRAFT'
  clearValidation()
  errorMessage.value = ''
  successMessage.value = ''
}

async function toggleOpen(activity) {
  errorMessage.value = ''
  successMessage.value = ''
  saving.value = true
  try {
    await updateAdminActivity(activity.id, {
      activityName: activity.activityName,
      visitDate: activity.visitDate,
      dailyCapacity: activity.dailyCapacity,
      personLimit: activity.personLimit,
      bookingStart: activity.bookingStart,
      bookingEnd: activity.bookingEnd,
      status: activity.status === 'OPEN' ? 'CLOSED' : 'OPEN'
    })
    successMessage.value = activity.status === 'OPEN' ? '活动已关闭' : '活动已开放'
    await loadActivities()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    saving.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.activityName = ''
  form.visitDate = ''
  form.dailyCapacity = 100
  form.personLimit = 1
  form.bookingStart = ''
  form.bookingEnd = ''
  form.status = 'DRAFT'
  clearValidation()
}

function validateForm() {
  clearValidation()

  if (!form.activityName) {
    validationErrors.activityName = '预约活动名称不能为空'
  }
  if (!form.visitDate) {
    validationErrors.visitDate = '参观日期不能为空'
  }
  if (!isPositiveNumber(form.dailyCapacity)) {
    validationErrors.dailyCapacity = '每日总名额必须大于 0'
  }
  if (!isPositiveNumber(form.personLimit)) {
    validationErrors.personLimit = '单人预约上限必须大于 0'
  }
  if (!form.bookingStart) {
    validationErrors.bookingStart = '预约开始时间不能为空'
  }
  if (!form.bookingEnd) {
    validationErrors.bookingEnd = '预约结束时间不能为空'
  }
  if (form.bookingStart && form.bookingEnd && form.bookingEnd <= form.bookingStart) {
    validationErrors.bookingEnd = '预约结束时间必须晚于开始时间'
  }
  if (!form.status) {
    validationErrors.status = '活动状态不能为空'
  }

  return Object.values(validationErrors).every((item) => !item)
}

function clearValidation() {
  Object.keys(validationErrors).forEach((key) => {
    validationErrors[key] = ''
  })
}

function buildRequestData() {
  return {
    activityName: form.activityName,
    visitDate: form.visitDate,
    dailyCapacity: Number(form.dailyCapacity),
    personLimit: Number(form.personLimit),
    bookingStart: form.bookingStart,
    bookingEnd: form.bookingEnd,
    status: form.status
  }
}

function toDateTimeInput(value) {
  return value ? String(value).slice(0, 16) : ''
}

function getStatusLabel(status) {
  return statusOptions.find((item) => item.value === status)?.label || status || '-'
}

function getStatusBadgeClass(status) {
  if (status === 'OPEN') {
    return 'text-bg-success'
  }
  if (status === 'CLOSED' || status === 'DISABLED') {
    return 'text-bg-secondary'
  }
  return 'text-bg-warning'
}
</script>

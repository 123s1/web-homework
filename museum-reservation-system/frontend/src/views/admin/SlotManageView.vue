<template>
  <AdminLayout>
    <div class="content-card p-4 p-lg-5 mb-4">
      <div class="row align-items-end g-4">
        <div class="col-xl-5">
          <span class="status-pill mb-3">预约时段</span>
          <h1 class="section-title mb-3">预约时段管理</h1>
          <p class="text-muted-strong mb-0">为预约活动配置入场时段、名额容量和启用状态。</p>
        </div>
        <div class="col-xl-7">
          <form class="row g-3" @submit.prevent="loadSlots">
            <div class="col-md-5">
              <label class="form-label fw-semibold" for="filterActivity">预约活动</label>
              <select id="filterActivity" v-model="filters.activityId" class="form-select rounded-4">
                <option value="">全部活动</option>
                <option v-for="activity in activities" :key="activity.id" :value="activity.id">
                  {{ activity.activityName }} / {{ formatDate(activity.visitDate) }}
                </option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold" for="filterVisitDate">参观日期</label>
              <input id="filterVisitDate" v-model="filters.visitDate" class="form-control rounded-4" type="date" />
            </div>
            <div class="col-md-3 d-grid align-self-end">
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
              <h2 class="h4 fw-bold mb-1">{{ editingId ? '编辑时段' : '新增时段' }}</h2>
              <div class="text-muted">{{ editingId ? '编辑时段名称、时间、容量和启用状态。' : '选择活动后创建新的入场时段。' }}</div>
            </div>
            <button v-if="editingId" class="btn btn-sm btn-outline-secondary rounded-pill" type="button" @click="resetForm">取消编辑</button>
          </div>

          <form class="row g-4" @submit.prevent="submitForm">
            <div class="col-12">
              <label class="form-label fw-semibold" for="activityId">关联活动</label>
              <select
                id="activityId"
                v-model="form.activityId"
                class="form-select rounded-4"
                :class="{ 'is-invalid': validationErrors.activityId }"
                :disabled="!!editingId"
              >
                <option value="">请选择预约活动</option>
                <option v-for="activity in activities" :key="activity.id" :value="activity.id">
                  {{ activity.activityName }} / {{ formatDate(activity.visitDate) }}
                </option>
              </select>
              <div class="invalid-feedback">{{ validationErrors.activityId }}</div>
              <div v-if="editingId" class="form-text">编辑时段时不能修改关联活动，如需调整请新建时段。</div>
            </div>

            <div class="col-12">
              <label class="form-label fw-semibold" for="slotName">时段名称</label>
              <input
                id="slotName"
                v-model.trim="form.slotName"
                class="form-control rounded-4"
                :class="{ 'is-invalid': validationErrors.slotName }"
                type="text"
                placeholder="例如：上午场"
              />
              <div class="invalid-feedback">{{ validationErrors.slotName }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="startTime">入场开始时间</label>
              <input id="startTime" v-model="form.startTime" class="form-control rounded-4" :class="{ 'is-invalid': validationErrors.startTime }" type="time" />
              <div class="invalid-feedback">{{ validationErrors.startTime }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="endTime">入场结束时间</label>
              <input id="endTime" v-model="form.endTime" class="form-control rounded-4" :class="{ 'is-invalid': validationErrors.endTime }" type="time" />
              <div class="invalid-feedback">{{ validationErrors.endTime }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="totalCapacity">时段总名额</label>
              <input id="totalCapacity" v-model.number="form.totalCapacity" class="form-control rounded-4" :class="{ 'is-invalid': validationErrors.totalCapacity }" type="number" min="1" />
              <div class="invalid-feedback">{{ validationErrors.totalCapacity }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="enabled">启用状态</label>
              <select id="enabled" v-model.number="form.enabled" class="form-select rounded-4">
                <option :value="1">已启用</option>
                <option :value="0">已停用</option>
              </select>
            </div>

            <div class="col-12 d-flex flex-wrap gap-3">
              <button class="btn btn-museum" type="submit" :disabled="saving">
                <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
                {{ saving ? '保存中...' : editingId ? '保存修改' : '新增时段' }}
              </button>
              <button class="btn btn-outline-museum" type="button" :disabled="saving" @click="resetForm">清空表单</button>
            </div>
          </form>
        </div>
      </div>

      <div class="col-xl-7">
        <LoadingState v-if="loading" />

        <div v-else-if="slots.length" class="d-flex flex-column gap-3">
          <article v-for="slot in slots" :key="slot.id" class="content-card p-4">
            <div class="d-flex flex-column flex-lg-row justify-content-between gap-3">
              <div class="flex-grow-1">
                <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                  <span class="badge rounded-pill" :class="slot.enabled === 1 ? 'text-bg-success' : 'text-bg-secondary'">
                    {{ slot.enabled === 1 ? '已启用' : '已停用' }}
                  </span>
                  <span class="badge rounded-pill text-bg-light">{{ formatDate(slot.visitDate) }}</span>
                  <span class="badge rounded-pill text-bg-light">活动 ID：{{ slot.activityId }}</span>
                </div>
                <h3 class="h5 fw-bold mb-3">{{ slot.slotName }}</h3>
                <div class="row g-3 mb-3">
                  <div class="col-md-4">
                    <div class="small text-muted">入场时间</div>
                    <div class="fw-bold">{{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}</div>
                  </div>
                  <div class="col-md-4">
                    <div class="small text-muted">总名额</div>
                    <div class="fw-bold">{{ slot.totalCapacity }}</div>
                  </div>
                  <div class="col-md-4">
                    <div class="small text-muted">剩余名额</div>
                    <div class="fw-bold text-danger">{{ slot.remaining }}</div>
                  </div>
                </div>
                <div class="progress rounded-pill mb-3" style="height: 10px">
                  <div class="progress-bar bg-danger" :style="{ width: getBookedPercent(slot) + '%' }"></div>
                </div>
                <div class="small text-muted">
                  已预约 {{ slot.bookedCount }} 人 / 版本 {{ slot.version }} / 更新：{{ formatDateTime(slot.updatedAt) }}
                </div>
              </div>
              <div class="d-flex flex-lg-column gap-2 align-self-start">
                <button class="btn btn-sm btn-outline-museum" type="button" @click="startEdit(slot)">编辑</button>
                <button class="btn btn-sm btn-outline-secondary" type="button" @click="toggleEnabled(slot)">
                  {{ slot.enabled === 1 ? '停用' : '启用' }}
                </button>
              </div>
            </div>
          </article>
        </div>

        <EmptyState v-else text="暂无预约时段数据，请新增时段或调整筛选条件" />
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
import { createAdminSlot, listAdminActivities, listAdminSlots, updateAdminSlot } from '../../api/admin'
import { formatDate, formatDateTime, formatTime } from '../../utils/format'
import { isPositiveNumber } from '../../utils/validators'

const filters = reactive({
  activityId: '',
  visitDate: ''
})

const form = reactive({
  activityId: '',
  slotName: '',
  startTime: '',
  endTime: '',
  totalCapacity: 100,
  enabled: 1
})

const validationErrors = reactive({
  activityId: '',
  slotName: '',
  startTime: '',
  endTime: '',
  totalCapacity: ''
})

const activities = ref([])
const slots = ref([])
const editingId = ref(null)
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

onMounted(async () => {
  await loadActivities()
  await loadSlots()
})

async function loadActivities() {
  errorMessage.value = ''
  try {
    activities.value = await listAdminActivities()
  } catch (error) {
    errorMessage.value = error.message
    activities.value = []
  }
}

async function loadSlots() {
  loading.value = true
  errorMessage.value = ''
  try {
    const params = {}
    if (filters.activityId) {
      params.activityId = Number(filters.activityId)
    }
    if (filters.visitDate) {
      params.visitDate = filters.visitDate
    }
    slots.value = await listAdminSlots(params)
  } catch (error) {
    errorMessage.value = error.message
    slots.value = []
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
    if (editingId.value) {
      await updateAdminSlot(editingId.value, buildUpdateData())
      successMessage.value = '预约时段修改成功'
    } else {
      await createAdminSlot(buildCreateData())
      successMessage.value = '预约时段新增成功'
    }
    resetForm()
    await loadSlots()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    saving.value = false
  }
}

function startEdit(slot) {
  editingId.value = slot.id
  form.activityId = slot.activityId || ''
  form.slotName = slot.slotName || ''
  form.startTime = toTimeInput(slot.startTime)
  form.endTime = toTimeInput(slot.endTime)
  form.totalCapacity = slot.totalCapacity || 100
  form.enabled = slot.enabled ?? 1
  clearValidation()
  errorMessage.value = ''
  successMessage.value = ''
}

async function toggleEnabled(slot) {
  errorMessage.value = ''
  successMessage.value = ''
  saving.value = true
  try {
    await updateAdminSlot(slot.id, {
      slotName: slot.slotName,
      startTime: toTimeInput(slot.startTime),
      endTime: toTimeInput(slot.endTime),
      totalCapacity: slot.totalCapacity,
      enabled: slot.enabled === 1 ? 0 : 1
    })
    successMessage.value = slot.enabled === 1 ? '预约时段已停用' : '预约时段已启用'
    await loadSlots()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    saving.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.activityId = ''
  form.slotName = ''
  form.startTime = ''
  form.endTime = ''
  form.totalCapacity = 100
  form.enabled = 1
  clearValidation()
}

function validateForm() {
  clearValidation()

  if (!editingId.value && !form.activityId) {
    validationErrors.activityId = '预约活动不能为空'
  }
  if (!form.slotName) {
    validationErrors.slotName = '时段名称不能为空'
  }
  if (!form.startTime) {
    validationErrors.startTime = '入场开始时间不能为空'
  }
  if (!form.endTime) {
    validationErrors.endTime = '入场结束时间不能为空'
  }
  if (form.startTime && form.endTime && form.endTime <= form.startTime) {
    validationErrors.endTime = '入场结束时间必须晚于开始时间'
  }
  if (!isPositiveNumber(form.totalCapacity)) {
    validationErrors.totalCapacity = '时段总名额必须大于 0'
  }

  return Object.values(validationErrors).every((item) => !item)
}

function clearValidation() {
  Object.keys(validationErrors).forEach((key) => {
    validationErrors[key] = ''
  })
}

function buildCreateData() {
  return {
    activityId: Number(form.activityId),
    slotName: form.slotName,
    startTime: form.startTime,
    endTime: form.endTime,
    totalCapacity: Number(form.totalCapacity),
    enabled: form.enabled
  }
}

function buildUpdateData() {
  return {
    slotName: form.slotName,
    startTime: form.startTime,
    endTime: form.endTime,
    totalCapacity: Number(form.totalCapacity),
    enabled: form.enabled
  }
}

function toTimeInput(value) {
  return value ? String(value).slice(0, 5) : ''
}

function getBookedPercent(slot) {
  if (!slot.totalCapacity) {
    return 0
  }
  return Math.min(100, Math.round((Number(slot.bookedCount) / Number(slot.totalCapacity)) * 100))
}
</script>

<template>
  <AdminLayout>
    <div class="content-card p-4 p-lg-5 mb-4">
      <div class="row align-items-end g-4">
        <div class="col-xl-4">
          <span class="status-pill mb-3">预约记录</span>
          <h1 class="section-title mb-3">预约名单查询与导出</h1>
          <p class="text-muted-strong mb-0">按日期、时段、身份证号和状态查询预约名单，并导出 CSV 文件。</p>
        </div>
        <div class="col-xl-8">
          <form class="row g-3" @submit.prevent="loadReservations">
            <div class="col-md-3">
              <label class="form-label fw-semibold" for="visitDate">参观日期</label>
              <input id="visitDate" v-model="filters.visitDate" class="form-control rounded-4" type="date" />
            </div>
            <div class="col-md-3">
              <label class="form-label fw-semibold" for="slotId">预约时段</label>
              <select id="slotId" v-model="filters.slotId" class="form-select rounded-4">
                <option value="">全部时段</option>
                <option v-for="slot in slots" :key="slot.id" :value="slot.id">
                  {{ formatDate(slot.visitDate) }} / {{ slot.slotName }}
                </option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label fw-semibold" for="idCard">身份证号</label>
              <input id="idCard" v-model.trim="filters.idCard" class="form-control rounded-4" :class="{ 'is-invalid': validationError }" maxlength="18" type="text" placeholder="可选" />
              <div class="invalid-feedback">{{ validationError }}</div>
            </div>
            <div class="col-md-3">
              <label class="form-label fw-semibold" for="status">预约状态</label>
              <select id="status" v-model="filters.status" class="form-select rounded-4">
                <option value="">全部状态</option>
                <option value="SUCCESS">预约成功</option>
                <option value="CANCELLED">已取消</option>
              </select>
            </div>
            <div class="col-12 d-flex flex-wrap gap-3 justify-content-xl-end">
              <button class="btn btn-museum" type="submit" :disabled="loading">查询记录</button>
              <button class="btn btn-outline-museum" type="button" :disabled="exporting" @click="exportCsv">
                <span v-if="exporting" class="spinner-border spinner-border-sm me-2"></span>
                {{ exporting ? '导出中...' : '导出 CSV' }}
              </button>
              <button class="btn btn-link text-decoration-none" type="button" @click="resetFilters">重置筛选</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <ErrorAlert :message="errorMessage" />
    <div v-if="successMessage" class="alert alert-success rounded-4">{{ successMessage }}</div>
    <div v-if="filters.idCard || filters.status" class="alert alert-warning rounded-4">
      CSV 导出接口仅支持参观日期和预约时段筛选，身份证号和状态筛选只影响页面查询结果。
    </div>

    <LoadingState v-if="loading" />

    <div v-else-if="reservations.length" class="content-card p-4">
      <div class="d-flex flex-column flex-md-row justify-content-between gap-3 mb-4">
        <div>
          <h2 class="h4 fw-bold mb-1">预约名单</h2>
          <div class="text-muted">共查询到 {{ reservations.length }} 条预约记录。</div>
        </div>
        <button class="btn btn-outline-museum align-self-start" type="button" :disabled="exporting" @click="exportCsv">导出当前日期/时段 CSV</button>
      </div>

      <div class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>预约编号</th>
              <th>游客姓名</th>
              <th>身份证号</th>
              <th>手机号</th>
              <th>参观日期</th>
              <th>时段</th>
              <th>状态</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="reservation in reservations" :key="reservation.reservationId">
              <td class="fw-semibold text-danger">{{ reservation.reservationNo }}</td>
              <td>{{ reservation.visitorName }}</td>
              <td>{{ reservation.idCard }}</td>
              <td>{{ reservation.phone }}</td>
              <td>{{ formatDate(reservation.visitDate) }}</td>
              <td>{{ reservation.slotName }}</td>
              <td>
                <span class="badge rounded-pill" :class="getStatusBadgeClass(reservation.status)">
                  {{ formatStatus(reservation.status) }}
                </span>
              </td>
              <td>{{ formatDateTime(reservation.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <EmptyState v-else text="暂无预约记录，请调整筛选条件后重试" />
  </AdminLayout>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { exportAdminReservations, listAdminReservations, listAdminSlots } from '../../api/admin'
import { formatDate, formatDateTime, formatStatus } from '../../utils/format'
import { isValidIdCard } from '../../utils/validators'

const filters = reactive({
  visitDate: '',
  slotId: '',
  idCard: '',
  status: ''
})

const slots = ref([])
const reservations = ref([])
const loading = ref(false)
const exporting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const validationError = ref('')

onMounted(async () => {
  await loadSlots()
  await loadReservations()
})

async function loadSlots() {
  errorMessage.value = ''
  try {
    slots.value = await listAdminSlots()
  } catch (error) {
    errorMessage.value = error.message
    slots.value = []
  }
}

async function loadReservations() {
  errorMessage.value = ''
  successMessage.value = ''
  validationError.value = ''

  if (filters.idCard && !isValidIdCard(filters.idCard)) {
    validationError.value = '身份证号格式不正确'
    reservations.value = []
    return
  }

  loading.value = true
  try {
    reservations.value = await listAdminReservations(buildQueryParams(true))
  } catch (error) {
    errorMessage.value = error.message
    reservations.value = []
  } finally {
    loading.value = false
  }
}

async function exportCsv() {
  errorMessage.value = ''
  successMessage.value = ''
  exporting.value = true
  try {
    const response = await exportAdminReservations(buildQueryParams(false))
    const blob = response.data instanceof Blob ? response.data : new Blob([response.data], { type: 'text/csv;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = getExportFileName()
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    successMessage.value = 'CSV 文件已开始下载'
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    exporting.value = false
  }
}

function resetFilters() {
  filters.visitDate = ''
  filters.slotId = ''
  filters.idCard = ''
  filters.status = ''
  validationError.value = ''
  loadReservations()
}

function buildQueryParams(includeAllFilters) {
  const params = {}
  if (filters.visitDate) {
    params.visitDate = filters.visitDate
  }
  if (filters.slotId) {
    params.slotId = Number(filters.slotId)
  }
  if (includeAllFilters && filters.idCard) {
    params.idCard = filters.idCard.toUpperCase()
  }
  if (includeAllFilters && filters.status) {
    params.status = filters.status
  }
  return params
}

function getExportFileName() {
  const datePart = filters.visitDate || 'all'
  const slotPart = filters.slotId ? `slot-${filters.slotId}` : 'all-slots'
  return `reservations_${datePart}_${slotPart}.csv`
}

function getStatusBadgeClass(status) {
  if (status === 'SUCCESS') {
    return 'text-bg-success'
  }
  if (status === 'CANCELLED') {
    return 'text-bg-secondary'
  }
  return 'text-bg-warning'
}
</script>

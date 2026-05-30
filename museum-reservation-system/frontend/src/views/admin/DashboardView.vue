<template>
  <AdminLayout>
    <div class="content-card p-4 p-lg-5 mb-4">
      <div class="row align-items-end g-4">
        <div class="col-lg-7">
          <span class="status-pill mb-3">后台首页</span>
          <h1 class="section-title mb-3">统计概览</h1>
          <p class="text-muted-strong mb-0">查看预约人数、容量使用情况和系统运行状态，可按参观日期筛选统计数据。</p>
        </div>
        <div class="col-lg-5">
          <form class="row g-3" @submit.prevent="loadDashboard">
            <div class="col-md-8">
              <label class="form-label fw-semibold" for="visitDate">参观日期</label>
              <input id="visitDate" v-model="visitDate" class="form-control rounded-4" type="date" />
            </div>
            <div class="col-md-4 d-grid align-self-end">
              <button class="btn btn-museum" type="submit" :disabled="loading">刷新</button>
            </div>
            <div class="col-12">
              <button class="btn btn-link px-0 text-decoration-none" type="button" @click="clearDate">查看全部日期统计</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <ErrorAlert :message="errorMessage" />
    <LoadingState v-if="loading" />

    <template v-else>
      <div class="row g-4 mb-4">
        <div v-for="card in metricCards" :key="card.label" class="col-md-6 col-xl-3">
          <div class="content-card p-4 h-100">
            <div class="small text-muted mb-2">{{ card.label }}</div>
            <div class="display-6 fw-bold mb-2">{{ card.value }}</div>
            <div class="text-muted-strong">{{ card.description }}</div>
          </div>
        </div>
      </div>

      <div class="row g-4">
        <div class="col-xl-8">
          <div class="content-card p-4 h-100">
            <div class="d-flex justify-content-between align-items-center gap-3 mb-4">
              <div>
                <h2 class="h4 fw-bold mb-1">分时段预约统计</h2>
                <div class="text-muted">展示各时段容量、已预约人数和剩余名额。</div>
              </div>
              <span class="badge text-bg-light rounded-pill">{{ summary?.slotSummary?.length || 0 }} 个时段</span>
            </div>

            <div v-if="summary?.slotSummary?.length" class="table-responsive">
              <table class="table align-middle">
                <thead>
                  <tr>
                    <th>参观日期</th>
                    <th>时段</th>
                    <th class="text-end">总名额</th>
                    <th class="text-end">已预约</th>
                    <th class="text-end">剩余</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="slot in summary.slotSummary" :key="slot.slotId">
                    <td>{{ formatDate(slot.visitDate) }}</td>
                    <td class="fw-semibold">{{ slot.slotName }}</td>
                    <td class="text-end">{{ slot.totalCapacity }}</td>
                    <td class="text-end">{{ slot.bookedCount }}</td>
                    <td class="text-end fw-bold text-danger">{{ slot.remaining }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <EmptyState v-else text="暂无分时段统计数据" />
          </div>
        </div>

        <div class="col-xl-4">
          <div class="content-card p-4 h-100">
            <h2 class="h4 fw-bold mb-4">系统运行状态</h2>
            <div class="d-flex justify-content-between align-items-center py-3 border-bottom">
              <span class="text-muted">系统状态</span>
              <span class="badge rounded-pill" :class="getHealthBadgeClass(status?.systemStatus)">
                {{ status?.systemStatus || '-' }}
              </span>
            </div>
            <div class="d-flex justify-content-between align-items-center py-3 border-bottom">
              <span class="text-muted">数据库状态</span>
              <span class="badge rounded-pill" :class="getHealthBadgeClass(status?.databaseStatus)">
                {{ status?.databaseStatus || '-' }}
              </span>
            </div>
            <div class="d-flex justify-content-between align-items-center py-3 border-bottom">
              <span class="text-muted">请求总数</span>
              <span class="fw-bold">{{ status?.totalRequestCount ?? '-' }}</span>
            </div>
            <div class="d-flex justify-content-between align-items-center py-3 border-bottom">
              <span class="text-muted">成功请求</span>
              <span class="fw-bold text-success">{{ status?.successRequestCount ?? '-' }}</span>
            </div>
            <div class="d-flex justify-content-between align-items-center py-3 border-bottom">
              <span class="text-muted">失败请求</span>
              <span class="fw-bold text-danger">{{ status?.failedRequestCount ?? '-' }}</span>
            </div>
            <div class="pt-3">
              <div class="text-muted mb-1">最后请求时间</div>
              <div class="fw-semibold">{{ formatDateTime(status?.lastRequestTime) }}</div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getAdminStatus, getAdminSummary } from '../../api/admin'
import { formatDate, formatDateTime } from '../../utils/format'

const visitDate = ref('')
const summary = ref(null)
const status = ref(null)
const loading = ref(false)
const errorMessage = ref('')

const metricCards = computed(() => [
  {
    label: '游客总数',
    value: summary.value?.visitorCount ?? 0,
    description: '已登记游客数量'
  },
  {
    label: '预约总数',
    value: summary.value?.reservationCount ?? 0,
    description: '当前筛选范围内的预约记录'
  },
  {
    label: '总容量',
    value: summary.value?.totalCapacity ?? 0,
    description: '可开放预约名额总数'
  },
  {
    label: '剩余名额',
    value: summary.value?.remaining ?? 0,
    description: `已预约 ${summary.value?.bookedCount ?? 0} 人`
  }
])

onMounted(() => {
  loadDashboard()
})

async function loadDashboard() {
  loading.value = true
  errorMessage.value = ''
  try {
    const params = visitDate.value ? { visitDate: visitDate.value } : {}
    const [summaryData, statusData] = await Promise.all([
      getAdminSummary(params),
      getAdminStatus()
    ])
    summary.value = summaryData
    status.value = statusData
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

function clearDate() {
  visitDate.value = ''
  loadDashboard()
}

function getHealthBadgeClass(value) {
  if (value === 'UP' || value === 'OK' || value === 'NORMAL') {
    return 'text-bg-success'
  }
  if (value === 'DOWN' || value === 'ERROR') {
    return 'text-bg-danger'
  }
  return 'text-bg-secondary'
}
</script>

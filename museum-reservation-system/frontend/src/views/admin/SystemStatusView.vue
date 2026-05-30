<template>
  <AdminLayout>
    <div class="content-card p-4 p-lg-5 mb-4">
      <div class="row align-items-end g-4">
        <div class="col-lg-8">
          <span class="status-pill mb-3">系统状态</span>
          <h1 class="section-title mb-3">系统状态查看</h1>
          <p class="text-muted-strong mb-0">查看系统健康状态、数据库连通性和接口请求统计，用于演示和运维检查。</p>
        </div>
        <div class="col-lg-4 text-lg-end">
          <button class="btn btn-museum" type="button" :disabled="loading" @click="loadStatus">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ loading ? '刷新中...' : '刷新状态' }}
          </button>
        </div>
      </div>
    </div>

    <ErrorAlert :message="errorMessage" />
    <LoadingState v-if="loading" />

    <template v-else>
      <div class="row g-4 mb-4">
        <div class="col-md-6 col-xl-3">
          <div class="content-card p-4 h-100">
            <div class="small text-muted mb-2">系统状态</div>
            <div class="d-flex align-items-center gap-3">
              <span class="status-dot" :class="getStatusDotClass(adminStatus?.systemStatus)"></span>
              <div class="display-6 fw-bold">{{ adminStatus?.systemStatus || '-' }}</div>
            </div>
            <div class="text-muted-strong mt-2">{{ getStatusText(adminStatus?.systemStatus) }}</div>
          </div>
        </div>

        <div class="col-md-6 col-xl-3">
          <div class="content-card p-4 h-100">
            <div class="small text-muted mb-2">数据库状态</div>
            <div class="d-flex align-items-center gap-3">
              <span class="status-dot" :class="getStatusDotClass(adminStatus?.databaseStatus)"></span>
              <div class="display-6 fw-bold">{{ adminStatus?.databaseStatus || '-' }}</div>
            </div>
            <div class="text-muted-strong mt-2">{{ getStatusText(adminStatus?.databaseStatus) }}</div>
          </div>
        </div>

        <div class="col-md-6 col-xl-3">
          <div class="content-card p-4 h-100">
            <div class="small text-muted mb-2">请求总数</div>
            <div class="display-6 fw-bold">{{ adminStatus?.totalRequestCount ?? 0 }}</div>
            <div class="text-muted-strong mt-2">累计记录的接口请求</div>
          </div>
        </div>

        <div class="col-md-6 col-xl-3">
          <div class="content-card p-4 h-100">
            <div class="small text-muted mb-2">成功率</div>
            <div class="display-6 fw-bold">{{ successRate }}%</div>
            <div class="text-muted-strong mt-2">成功请求占比</div>
          </div>
        </div>
      </div>

      <div class="row g-4">
        <div class="col-xl-7">
          <div class="content-card p-4 p-lg-5 h-100">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <div>
                <h2 class="h4 fw-bold mb-1">请求统计</h2>
                <div class="text-muted">来自后端请求日志表的统计结果。</div>
              </div>
              <span class="badge rounded-pill" :class="getStatusBadgeClass(adminStatus?.systemStatus)">
                {{ getStatusText(adminStatus?.systemStatus) }}
              </span>
            </div>

            <div class="row g-4 mb-4">
              <div class="col-md-6">
                <div class="border rounded-4 p-4 bg-light h-100">
                  <div class="small text-muted mb-1">成功请求数</div>
                  <div class="fs-2 fw-bold text-success">{{ adminStatus?.successRequestCount ?? 0 }}</div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="border rounded-4 p-4 bg-light h-100">
                  <div class="small text-muted mb-1">失败请求数</div>
                  <div class="fs-2 fw-bold text-danger">{{ adminStatus?.failedRequestCount ?? 0 }}</div>
                </div>
              </div>
            </div>

            <div class="progress rounded-pill mb-4" style="height: 14px">
              <div class="progress-bar bg-success" :style="{ width: successRate + '%' }"></div>
            </div>

            <div class="d-flex justify-content-between align-items-center py-3 border-top">
              <span class="text-muted">最后请求时间</span>
              <span class="fw-semibold">{{ formatDateTime(adminStatus?.lastRequestTime) }}</span>
            </div>
            <div class="d-flex justify-content-between align-items-center py-3 border-top">
              <span class="text-muted">本次刷新时间</span>
              <span class="fw-semibold">{{ formatDateTime(refreshedAt) }}</span>
            </div>
          </div>
        </div>

        <div class="col-xl-5">
          <div class="content-card p-4 p-lg-5 h-100">
            <h2 class="h4 fw-bold mb-4">健康检查接口</h2>

            <div class="d-flex justify-content-between align-items-center py-3 border-bottom">
              <span class="text-muted">健康状态</span>
              <span class="badge rounded-pill" :class="getStatusBadgeClass(healthStatus?.status)">
                {{ healthStatus?.status || '-' }}
              </span>
            </div>
            <div class="d-flex justify-content-between align-items-center py-3 border-bottom">
              <span class="text-muted">数据库</span>
              <span class="badge rounded-pill" :class="getStatusBadgeClass(healthStatus?.database)">
                {{ healthStatus?.database || '-' }}
              </span>
            </div>
            <div class="d-flex justify-content-between align-items-center py-3 border-bottom">
              <span class="text-muted">健康检查时间</span>
              <span class="fw-semibold">{{ formatDateTime(healthStatus?.time) }}</span>
            </div>

            <div class="alert alert-warning rounded-4 mt-4 mb-0">
              如果数据库状态为 DOWN，请优先检查后端数据库环境变量和 MySQL 服务状态。
            </div>
          </div>
        </div>
      </div>
    </template>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getAdminStatus, getHealthStatus } from '../../api/admin'
import { formatDateTime } from '../../utils/format'

const adminStatus = ref(null)
const healthStatus = ref(null)
const refreshedAt = ref(null)
const loading = ref(false)
const errorMessage = ref('')

const successRate = computed(() => {
  const total = Number(adminStatus.value?.totalRequestCount || 0)
  const success = Number(adminStatus.value?.successRequestCount || 0)
  if (!total) {
    return 0
  }
  return Math.round((success / total) * 100)
})

onMounted(() => {
  loadStatus()
})

async function loadStatus() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [adminStatusData, healthStatusData] = await Promise.all([
      getAdminStatus(),
      getHealthStatus()
    ])
    adminStatus.value = adminStatusData
    healthStatus.value = healthStatusData
    refreshedAt.value = new Date().toISOString()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

function getStatusText(value) {
  if (value === 'UP') {
    return '运行正常'
  }
  if (value === 'DOWN') {
    return '运行异常'
  }
  return '状态未知'
}

function getStatusBadgeClass(value) {
  if (value === 'UP') {
    return 'text-bg-success'
  }
  if (value === 'DOWN') {
    return 'text-bg-danger'
  }
  return 'text-bg-secondary'
}

function getStatusDotClass(value) {
  if (value === 'UP') {
    return 'status-dot-up'
  }
  if (value === 'DOWN') {
    return 'status-dot-down'
  }
  return 'status-dot-unknown'
}
</script>

<style scoped>
.status-dot {
  width: 18px;
  height: 18px;
  border-radius: 999px;
  display: inline-block;
}

.status-dot-up {
  background: #198754;
  box-shadow: 0 0 0 8px rgba(25, 135, 84, 0.14);
}

.status-dot-down {
  background: #dc3545;
  box-shadow: 0 0 0 8px rgba(220, 53, 69, 0.14);
}

.status-dot-unknown {
  background: #6c757d;
  box-shadow: 0 0 0 8px rgba(108, 117, 125, 0.14);
}
</style>

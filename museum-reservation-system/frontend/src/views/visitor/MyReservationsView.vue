<template>
  <VisitorLayout>
    <section class="placeholder-page">
      <div class="container">
        <div class="content-card p-4 p-lg-5 mb-4">
          <div class="row align-items-end g-4">
            <div class="col-lg-7">
              <span class="status-pill mb-3">我的预约</span>
              <h1 class="section-title mb-3">查询我的预约记录</h1>
              <p class="text-muted-strong mb-0">输入预约时使用的身份证号，可查询历史预约记录和入馆凭证内容。</p>
            </div>
            <div class="col-lg-5">
              <form class="row g-3" @submit.prevent="loadReservations">
                <div class="col-12">
                  <label class="form-label fw-semibold" for="idCard">身份证号</label>
                  <input
                    id="idCard"
                    v-model.trim="idCard"
                    class="form-control rounded-4"
                    :class="{ 'is-invalid': validationError }"
                    maxlength="18"
                    type="text"
                    placeholder="请输入 18 位身份证号"
                  />
                  <div class="invalid-feedback">{{ validationError }}</div>
                </div>
                <div class="col-12 d-flex flex-wrap gap-3">
                  <button class="btn btn-museum" type="submit" :disabled="loading">
                    <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                    {{ loading ? '查询中...' : '查询预约' }}
                  </button>
                  <RouterLink class="btn btn-outline-museum" to="/visitor/login">切换游客</RouterLink>
                  <RouterLink class="btn btn-link text-decoration-none" to="/slots">去预约</RouterLink>
                </div>
              </form>
            </div>
          </div>
        </div>

        <div v-if="visitorInfo" class="alert alert-warning rounded-4">
          当前已保存游客信息：{{ visitorInfo.name }}，可直接查询身份证号 {{ visitorInfo.idCard }} 的预约记录。
        </div>

        <ErrorAlert :message="errorMessage" />
        <LoadingState v-if="loading" />

        <div v-else-if="reservations.length" class="row g-4">
          <div v-for="reservation in reservations" :key="reservation.reservationId" class="col-lg-6">
            <article class="content-card p-4 h-100">
              <div class="d-flex justify-content-between align-items-start gap-3 mb-4">
                <div>
                  <div class="small text-muted">预约编号</div>
                  <h2 class="h5 fw-bold text-danger mb-0">{{ reservation.reservationNo }}</h2>
                </div>
                <span class="badge rounded-pill" :class="getStatusBadgeClass(reservation.status)">
                  {{ formatStatus(reservation.status) }}
                </span>
              </div>

              <div class="row g-3 mb-4">
                <div class="col-md-6">
                  <div class="small text-muted">游客姓名</div>
                  <div class="fw-semibold">{{ reservation.visitorName }}</div>
                </div>
                <div class="col-md-6">
                  <div class="small text-muted">手机号</div>
                  <div class="fw-semibold">{{ reservation.phone }}</div>
                </div>
                <div class="col-md-6">
                  <div class="small text-muted">参观日期</div>
                  <div class="fw-semibold">{{ formatDate(reservation.visitDate) }}</div>
                </div>
                <div class="col-md-6">
                  <div class="small text-muted">入场时段</div>
                  <div class="fw-semibold">{{ reservation.slotName }}</div>
                </div>
                <div class="col-md-6">
                  <div class="small text-muted">创建时间</div>
                  <div class="fw-semibold">{{ formatDateTime(reservation.createdAt) }}</div>
                </div>
                <div class="col-md-6">
                  <div class="small text-muted">身份证号</div>
                  <div class="fw-semibold">{{ reservation.idCard }}</div>
                </div>
              </div>

              <div class="border rounded-4 p-3 bg-light d-flex align-items-center gap-3">
                <div class="bg-white p-2 rounded-3 border flex-shrink-0">
                  <QrCode :value="reservation.qrContent" :size="120" />
                </div>
                <div>
                  <div class="small text-muted mb-1">入馆凭证二维码</div>
                  <div class="small text-break">{{ reservation.qrContent }}</div>
                </div>
              </div>
            </article>
          </div>
        </div>

        <EmptyState v-else text="暂无预约记录，请确认身份证号是否正确，或先完成预约" />
      </div>
    </section>
  </VisitorLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'
import QrCode from '../../components/QrCode.vue'
import VisitorLayout from '../../layouts/VisitorLayout.vue'
import { listMyReservations } from '../../api/reservation'
import { getVisitorInfo } from '../../utils/storage'
import { formatDate, formatDateTime, formatStatus } from '../../utils/format'
import { isValidIdCard } from '../../utils/validators'

const visitorInfo = ref(getVisitorInfo())
const idCard = ref(visitorInfo.value?.idCard || '')
const reservations = ref([])
const loading = ref(false)
const errorMessage = ref('')
const validationError = ref('')

onMounted(() => {
  if (idCard.value) {
    loadReservations()
  }
})

async function loadReservations() {
  errorMessage.value = ''
  validationError.value = ''

  if (!isValidIdCard(idCard.value)) {
    validationError.value = '身份证号格式不正确'
    reservations.value = []
    return
  }

  loading.value = true
  try {
    reservations.value = await listMyReservations(idCard.value.toUpperCase())
  } catch (error) {
    errorMessage.value = error.message
    reservations.value = []
  } finally {
    loading.value = false
  }
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

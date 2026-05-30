<template>
  <VisitorLayout>
    <section class="placeholder-page">
      <div class="container">
        <div class="content-card p-4 p-lg-5 mb-4">
          <div class="row align-items-end g-4">
            <div class="col-lg-7">
              <span class="status-pill mb-3">预约时段</span>
              <h1 class="section-title mb-3">选择参观日期与入场时段</h1>
              <p class="text-muted-strong mb-0">请先完成游客实名登录，再选择有剩余名额的开放时段提交预约。</p>
            </div>
            <div class="col-lg-5">
              <form class="row g-3" @submit.prevent="loadSlots">
                <div class="col-md-8">
                  <label class="form-label fw-semibold" for="visitDate">参观日期</label>
                  <input id="visitDate" v-model="selectedDate" class="form-control rounded-4" type="date" />
                </div>
                <div class="col-md-4 d-grid align-self-end">
                  <button class="btn btn-museum" type="submit" :disabled="loading">查询</button>
                </div>
                <div class="col-12 d-flex justify-content-between align-items-center">
                  <button class="btn btn-link px-0 text-decoration-none" type="button" @click="clearDate">查看全部时段</button>
                  <RouterLink class="btn btn-link px-0 text-decoration-none" to="/visitor/login">
                    {{ visitorInfo ? '切换游客' : '先登录游客' }}
                  </RouterLink>
                </div>
              </form>
            </div>
          </div>
        </div>

        <div v-if="visitorInfo" class="alert alert-warning rounded-4">
          当前预约人：{{ visitorInfo.name }}，身份证号 {{ visitorInfo.idCard }}
        </div>

        <ErrorAlert :message="errorMessage" />
        <LoadingState v-if="loading" />

        <div v-else-if="slots.length" class="row g-4">
          <div v-for="slot in slots" :key="slot.slotId" class="col-lg-6 col-xl-4">
            <article class="content-card h-100 p-4 d-flex flex-column">
              <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                <div>
                  <div class="small text-muted">参观日期</div>
                  <h2 class="h5 fw-bold mb-0">{{ formatDate(slot.visitDate) }}</h2>
                </div>
                <span class="badge rounded-pill" :class="getSlotBadgeClass(slot)">
                  {{ getSlotStatusText(slot) }}
                </span>
              </div>

              <div class="mb-3">
                <div class="small text-muted">预约活动</div>
                <div class="fw-semibold">{{ slot.activityName }}</div>
              </div>

              <div class="mb-3">
                <div class="small text-muted">入场时段</div>
                <div class="fw-semibold">{{ slot.slotName }} · {{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}</div>
              </div>

              <div class="row g-3 mb-4">
                <div class="col-4">
                  <div class="small text-muted">总名额</div>
                  <div class="fs-5 fw-bold">{{ slot.totalCapacity }}</div>
                </div>
                <div class="col-4">
                  <div class="small text-muted">已预约</div>
                  <div class="fs-5 fw-bold">{{ slot.bookedCount }}</div>
                </div>
                <div class="col-4">
                  <div class="small text-muted">剩余</div>
                  <div class="fs-5 fw-bold text-danger">{{ slot.remaining }}</div>
                </div>
              </div>

              <div class="progress rounded-pill mb-4" style="height: 10px">
                <div class="progress-bar bg-danger" :style="{ width: getBookedPercent(slot) + '%' }"></div>
              </div>

              <div class="small text-muted mb-4">
                预约开放时间：{{ formatDateTime(slot.bookingStart) }} 至 {{ formatDateTime(slot.bookingEnd) }}
              </div>

              <div class="mt-auto d-grid">
                <button class="btn btn-museum" type="button" :disabled="bookingSlotId === slot.slotId || !isBookable(slot)" @click="submitReservation(slot)">
                  <span v-if="bookingSlotId === slot.slotId" class="spinner-border spinner-border-sm me-2"></span>
                  {{ bookingSlotId === slot.slotId ? '提交中...' : '预约该时段' }}
                </button>
              </div>
            </article>
          </div>
        </div>

        <EmptyState v-else text="当前没有可预约时段，请更换日期后再试" />
      </div>
    </section>
  </VisitorLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'
import VisitorLayout from '../../layouts/VisitorLayout.vue'
import { listAvailableSlots } from '../../api/museum'
import { createReservation } from '../../api/reservation'
import { getVisitorInfo, setReservationResult } from '../../utils/storage'
import { formatDate, formatDateTime, formatTime } from '../../utils/format'

const router = useRouter()
const selectedDate = ref('')
const slots = ref([])
const loading = ref(false)
const bookingSlotId = ref(null)
const errorMessage = ref('')
const visitorInfo = ref(getVisitorInfo())

onMounted(() => {
  loadSlots()
})

async function loadSlots() {
  loading.value = true
  errorMessage.value = ''
  try {
    const params = selectedDate.value ? { visitDate: selectedDate.value } : {}
    slots.value = await listAvailableSlots(params)
  } catch (error) {
    errorMessage.value = error.message
    slots.value = []
  } finally {
    loading.value = false
  }
}

function clearDate() {
  selectedDate.value = ''
  loadSlots()
}

async function submitReservation(slot) {
  errorMessage.value = ''
  visitorInfo.value = getVisitorInfo()
  if (!visitorInfo.value) {
    errorMessage.value = '请先完成游客实名登录，再提交预约'
    setTimeout(() => {
      router.push('/visitor/login')
    }, 600)
    return
  }
  const confirmed = window.confirm(`确认预约 ${formatDate(slot.visitDate)} ${slot.slotName} 吗？`)
  if (!confirmed) {
    return
  }

  bookingSlotId.value = slot.slotId
  try {
    const result = await createReservation({
      name: visitorInfo.value.name,
      idCard: visitorInfo.value.idCard,
      phone: visitorInfo.value.phone,
      slotId: slot.slotId
    })
    setReservationResult(result)
    router.push('/reservation/result')
  } catch (error) {
    errorMessage.value = error.message
    loadSlots()
  } finally {
    bookingSlotId.value = null
  }
}

function isBookable(slot) {
  return slot.enabled === 1 && slot.activityStatus === 'OPEN' && Number(slot.remaining) > 0
}

function getSlotStatusText(slot) {
  if (slot.enabled !== 1) {
    return '时段停用'
  }
  if (slot.activityStatus !== 'OPEN') {
    return '暂未开放'
  }
  if (Number(slot.remaining) <= 0) {
    return '名额已满'
  }
  return '可预约'
}

function getSlotBadgeClass(slot) {
  if (isBookable(slot)) {
    return 'text-bg-success'
  }
  if (Number(slot.remaining) <= 0) {
    return 'text-bg-secondary'
  }
  return 'text-bg-warning'
}

function getBookedPercent(slot) {
  if (!slot.totalCapacity) {
    return 0
  }
  return Math.min(100, Math.round((Number(slot.bookedCount) / Number(slot.totalCapacity)) * 100))
}
</script>

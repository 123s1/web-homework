<template>
  <VisitorLayout>
    <section class="placeholder-page">
      <div class="container">
        <div v-if="reservation" class="row justify-content-center">
          <div class="col-xl-9">
            <div class="content-card p-4 p-lg-5">
              <div class="d-flex flex-column flex-md-row justify-content-between gap-4 mb-4">
                <div>
                  <span class="status-pill mb-3">预约成功</span>
                  <h1 class="section-title mb-3">请保存您的预约凭证</h1>
                  <p class="text-muted-strong mb-0">入馆时请携带有效身份证件，并按预约日期和时段到馆参观。</p>
                </div>
                <div class="text-md-end">
                  <div class="small text-muted">预约编号</div>
                  <div class="h4 fw-bold text-danger mb-0">{{ reservation.reservationNo }}</div>
                </div>
              </div>

              <div class="row g-4 mb-4">
                <div class="col-md-6">
                  <div class="border rounded-4 p-4 h-100 bg-light">
                    <div class="small text-muted mb-1">游客姓名</div>
                    <div class="fs-5 fw-bold">{{ reservation.visitorName }}</div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="border rounded-4 p-4 h-100 bg-light">
                    <div class="small text-muted mb-1">身份证号</div>
                    <div class="fs-5 fw-bold">{{ reservation.idCard }}</div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="border rounded-4 p-4 h-100 bg-light">
                    <div class="small text-muted mb-1">手机号</div>
                    <div class="fs-5 fw-bold">{{ reservation.phone }}</div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="border rounded-4 p-4 h-100 bg-light">
                    <div class="small text-muted mb-1">预约时间</div>
                    <div class="fs-5 fw-bold">{{ formatDateTime(reservation.createdAt) }}</div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="border rounded-4 p-4 h-100 bg-light">
                    <div class="small text-muted mb-1">参观日期</div>
                    <div class="fs-5 fw-bold">{{ formatDate(reservation.visitDate) }}</div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="border rounded-4 p-4 h-100 bg-light">
                    <div class="small text-muted mb-1">入场时段</div>
                    <div class="fs-5 fw-bold">{{ reservation.slotName }} · {{ formatTime(reservation.startTime) }} - {{ formatTime(reservation.endTime) }}</div>
                  </div>
                </div>
              </div>

              <div class="border rounded-4 p-4 p-lg-5 text-center mb-4">
                <div class="small text-muted mb-3">入馆凭证二维码</div>
                <div class="mx-auto mb-3 qr-code-box">
                  <QrCode :value="reservation.qrContent" :size="220" />
                </div>
                <p class="text-muted-strong mb-1">入馆时请向工作人员出示此二维码核验。</p>
                <div class="small text-muted text-break">{{ reservation.qrContent }}</div>
              </div>

              <div class="d-flex flex-wrap gap-3">
                <RouterLink class="btn btn-museum" to="/reservations/my">查看我的预约</RouterLink>
                <RouterLink class="btn btn-outline-museum" to="/slots">继续预约其他时段</RouterLink>
                <RouterLink class="btn btn-link text-decoration-none" to="/">返回首页</RouterLink>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="content-card p-5 text-center">
          <span class="status-pill mb-3">暂无预约结果</span>
          <h1 class="section-title mb-3">未找到最近一次预约凭证</h1>
          <p class="text-muted-strong mb-4">请先在预约时段页面提交预约，或前往“我的预约”查询历史预约记录。</p>
          <div class="d-flex justify-content-center flex-wrap gap-3">
            <RouterLink class="btn btn-museum" to="/slots">去预约</RouterLink>
            <RouterLink class="btn btn-outline-museum" to="/reservations/my">查询我的预约</RouterLink>
          </div>
        </div>
      </div>
    </section>
  </VisitorLayout>
</template>

<script setup>
import { ref } from 'vue'
import VisitorLayout from '../../layouts/VisitorLayout.vue'
import QrCode from '../../components/QrCode.vue'
import { getReservationResult } from '../../utils/storage'
import { formatDate, formatDateTime, formatTime } from '../../utils/format'

const reservation = ref(getReservationResult())
</script>

<style scoped>
.qr-code-box {
  width: fit-content;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
}
</style>

<template>
  <VisitorLayout>
    <CarouselSection />

    <section class="py-5">
      <div class="container">
        <ErrorAlert :message="errorMessage" />
        <LoadingState v-if="loading" />
        <template v-else>
          <div class="d-flex justify-content-between align-items-end mb-4">
            <div>
              <span class="status-pill mb-2">公告通知</span>
              <h2 class="section-title mb-0">参观公告与预约规则</h2>
            </div>
            <RouterLink class="btn btn-outline-museum d-none d-md-inline-flex" to="/slots">查看可预约时段</RouterLink>
          </div>
          <div v-if="notices.length" class="row g-4">
            <div v-for="notice in notices" :key="notice.id" class="col-md-6 col-xl-4">
              <article class="content-card h-100 p-4">
                <div class="d-flex justify-content-between gap-3 mb-3">
                  <span class="badge text-bg-warning rounded-pill">{{ notice.type }}</span>
                  <span class="small text-muted">{{ formatDateTime(notice.createdAt) }}</span>
                </div>
                <h3 class="h5 fw-bold mb-3">{{ notice.title }}</h3>
                <p class="text-muted-strong mb-0">{{ notice.content }}</p>
              </article>
            </div>
          </div>
          <EmptyState v-else text="当前暂无启用公告" />
        </template>
      </div>
    </section>

    <section class="pb-5">
      <div class="container">
        <div class="row g-4">
          <div class="col-md-4">
            <RouterLink to="/slots" class="feature-card text-decoration-none d-block h-100 p-4 p-lg-5">
              <div class="feature-icon mb-3">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M8 3.5a.5.5 0 0 0-1 0V9a.5.5 0 0 0 .252.434l3.5 2a.5.5 0 0 0 .496-.868L8 8.71V3.5z"/>
                  <path d="M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16zm7-8A7 7 0 1 1 1 8a7 7 0 0 1 14 0z"/>
                </svg>
              </div>
              <h3 class="h5 fw-bold section-title mb-2">参观预约</h3>
              <p class="text-muted-strong mb-0">选择参观日期与时段，在线完成实名预约，轻松入馆。</p>
            </RouterLink>
          </div>
          <div class="col-md-4">
            <RouterLink to="/reservations/my" class="feature-card text-decoration-none d-block h-100 p-4 p-lg-5">
              <div class="feature-icon mb-3">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M5.854 4.854a.5.5 0 1 0-.708-.708l-3.5 3.5a.5.5 0 0 0 0 .708l3.5 3.5a.5.5 0 0 0 .708-.708L2.707 8l3.147-3.146zm4.292 0a.5.5 0 0 1 .708-.708l3.5 3.5a.5.5 0 0 1 0 .708l-3.5 3.5a.5.5 0 0 1-.708-.708L13.293 8l-3.147-3.146z"/>
                </svg>
              </div>
              <h3 class="h5 fw-bold section-title mb-2">预约查询</h3>
              <p class="text-muted-strong mb-0">输入预约信息，随时查看预约状态与参观详情。</p>
            </RouterLink>
          </div>
          <div class="col-md-4">
            <div class="feature-card h-100 p-4 p-lg-5">
              <div class="feature-icon mb-3">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M0 2a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V2zm8.5 4.5a.5.5 0 0 0-1 0v3.793L6.354 9.146a.5.5 0 1 0-.708.708l2 2a.5.5 0 0 0 .708 0l2-2a.5.5 0 0 0-.708-.708L8.5 10.293V6.5z"/>
                </svg>
              </div>
              <h3 class="h5 fw-bold section-title mb-2">场馆信息</h3>
              <div v-if="museumInfo">
                <div class="mb-2">
                  <span class="small text-muted">场馆地址</span>
                  <div class="fw-semibold">{{ museumInfo.address || '-' }}</div>
                </div>
                <div class="mb-2">
                  <span class="small text-muted">开放说明</span>
                  <div class="fw-semibold">{{ museumInfo.openInfo || '-' }}</div>
                </div>
                <div>
                  <span class="small text-muted">参观须知</span>
                  <div class="text-muted-strong white-space-pre-line small">{{ museumInfo.rules || '-' }}</div>
                </div>
              </div>
              <div v-else class="text-muted-strong">暂无场馆信息</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <CollectionSection />
  </VisitorLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import CarouselSection from '../../components/CarouselSection.vue'
import CollectionSection from '../../components/CollectionSection.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'
import VisitorLayout from '../../layouts/VisitorLayout.vue'
import { getMuseumInfo, listNotices } from '../../api/museum'
import { formatDateTime } from '../../utils/format'

const loading = ref(true)
const errorMessage = ref('')
const museumInfo = ref(null)
const notices = ref([])

onMounted(async () => {
  try {
    const [museum, noticeList] = await Promise.all([getMuseumInfo(), listNotices()])
    museumInfo.value = museum
    notices.value = noticeList || []
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
})
</script>

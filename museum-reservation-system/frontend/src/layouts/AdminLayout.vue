<template>
  <div class="app-shell">
    <nav class="navbar navbar-dark museum-navbar sticky-top">
      <div class="container-fluid px-4">
        <RouterLink class="navbar-brand d-flex align-items-center gap-2 fw-bold" to="/admin">
          <span class="brand-mark">管</span>
          <span>预约管理后台</span>
        </RouterLink>
        <div class="d-flex align-items-center gap-3 text-white-50 small">
          <span>{{ adminName }}</span>
          <button class="btn btn-sm btn-outline-light rounded-pill px-3" @click="logout">退出</button>
        </div>
      </div>
    </nav>
    <div class="container-fluid main-content">
      <div class="row min-vh-100">
        <aside class="col-lg-2 border-end bg-white py-4">
          <div class="nav flex-column nav-pills gap-1">
            <RouterLink class="nav-link" to="/admin">统计概览</RouterLink>
            <RouterLink class="nav-link" to="/admin/museum">场馆信息</RouterLink>
            <RouterLink class="nav-link" to="/admin/notices">公告管理</RouterLink>
            <RouterLink class="nav-link" to="/admin/activities">预约活动</RouterLink>
            <RouterLink class="nav-link" to="/admin/slots">预约时段</RouterLink>
            <RouterLink class="nav-link" to="/admin/reservations">预约记录</RouterLink>
            <RouterLink class="nav-link" to="/admin/status">系统状态</RouterLink>
          </div>
        </aside>
        <main class="col-lg-10 py-4 px-lg-5">
          <slot />
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { clearAdminInfo, getAdminInfo } from '../utils/storage'

const router = useRouter()
const adminName = computed(() => getAdminInfo()?.username || '管理员')

function logout() {
  clearAdminInfo()
  router.push('/admin/login')
}
</script>

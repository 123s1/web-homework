import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/visitor/HomeView.vue'
import VisitorLoginView from '../views/visitor/VisitorLoginView.vue'
import SlotListView from '../views/visitor/SlotListView.vue'
import ReservationResultView from '../views/visitor/ReservationResultView.vue'
import MyReservationsView from '../views/visitor/MyReservationsView.vue'
import AdminLoginView from '../views/admin/AdminLoginView.vue'
import DashboardView from '../views/admin/DashboardView.vue'
import MuseumManageView from '../views/admin/MuseumManageView.vue'
import NoticeManageView from '../views/admin/NoticeManageView.vue'
import ActivityManageView from '../views/admin/ActivityManageView.vue'
import SlotManageView from '../views/admin/SlotManageView.vue'
import ReservationManageView from '../views/admin/ReservationManageView.vue'
import SystemStatusView from '../views/admin/SystemStatusView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/visitor/login', name: 'visitor-login', component: VisitorLoginView },
  { path: '/slots', name: 'slots', component: SlotListView },
  { path: '/reservation/result', name: 'reservation-result', component: ReservationResultView },
  { path: '/reservations/my', name: 'my-reservations', component: MyReservationsView },
  { path: '/admin/login', name: 'admin-login', component: AdminLoginView },
  { path: '/admin', name: 'admin-dashboard', component: DashboardView },
  { path: '/admin/museum', name: 'admin-museum', component: MuseumManageView },
  { path: '/admin/notices', name: 'admin-notices', component: NoticeManageView },
  { path: '/admin/activities', name: 'admin-activities', component: ActivityManageView },
  { path: '/admin/slots', name: 'admin-slots', component: SlotManageView },
  { path: '/admin/reservations', name: 'admin-reservations', component: ReservationManageView },
  { path: '/admin/status', name: 'admin-status', component: SystemStatusView }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
    const adminInfo = localStorage.getItem('adminInfo')
    if (!adminInfo) {
      return { name: 'admin-login' }
    }
  }
})

export default router

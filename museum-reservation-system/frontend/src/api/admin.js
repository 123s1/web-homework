import request from './request'

export function loginAdmin(data) {
  return request.post('/admin/login', data)
}

export function getAdminSummary(params = {}) {
  return request.get('/admin/summary', { params })
}

export function getAdminStatus() {
  return request.get('/admin/status')
}

export function getHealthStatus() {
  return request.get('/health')
}

export function getAdminMuseumInfo() {
  return request.get('/admin/museum/info')
}

export function updateAdminMuseumInfo(data) {
  return request.put('/admin/museum/info', data)
}

export function listAdminNotices(params = {}) {
  return request.get('/admin/notices', { params })
}

export function createAdminNotice(data) {
  return request.post('/admin/notices', data)
}

export function updateAdminNotice(id, data) {
  return request.put(`/admin/notices/${id}`, data)
}

export function deleteAdminNotice(id) {
  return request.delete(`/admin/notices/${id}`)
}

export function listAdminActivities(params = {}) {
  return request.get('/admin/activities', { params })
}

export function createAdminActivity(data) {
  return request.post('/admin/activities', data)
}

export function updateAdminActivity(id, data) {
  return request.put(`/admin/activities/${id}`, data)
}

export function listAdminSlots(params = {}) {
  return request.get('/admin/slots', { params })
}

export function createAdminSlot(data) {
  return request.post('/admin/slots', data)
}

export function updateAdminSlot(id, data) {
  return request.put(`/admin/slots/${id}`, data)
}

export function listAdminReservations(params = {}) {
  return request.get('/admin/reservations', { params })
}

export function exportAdminReservations(params = {}) {
  return request.get('/admin/reservations/export', { params, responseType: 'blob' })
}

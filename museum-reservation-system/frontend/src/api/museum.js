import request from './request'

export function getMuseumInfo() {
  return request.get('/museum/info')
}

export function listNotices() {
  return request.get('/notices')
}

export function listAvailableSlots(params = {}) {
  return request.get('/slots', { params })
}

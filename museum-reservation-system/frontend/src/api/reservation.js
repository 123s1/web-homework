import request from './request'

export function createReservation(data) {
  return request.post('/reservations', data)
}

export function listMyReservations(idCard) {
  return request.get('/reservations/my', { params: { idCard } })
}

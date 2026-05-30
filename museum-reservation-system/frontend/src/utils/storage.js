const VISITOR_KEY = 'visitorInfo'
const ADMIN_KEY = 'adminInfo'
const RESERVATION_RESULT_KEY = 'reservationResult'

export function getVisitorInfo() {
  return readJson(VISITOR_KEY)
}

export function setVisitorInfo(value) {
  localStorage.setItem(VISITOR_KEY, JSON.stringify(value))
}

export function clearVisitorInfo() {
  localStorage.removeItem(VISITOR_KEY)
}

export function getAdminInfo() {
  return readJson(ADMIN_KEY)
}

export function setAdminInfo(value) {
  localStorage.setItem(ADMIN_KEY, JSON.stringify(value))
}

export function clearAdminInfo() {
  localStorage.removeItem(ADMIN_KEY)
}

export function getReservationResult() {
  return readSessionJson(RESERVATION_RESULT_KEY)
}

export function setReservationResult(value) {
  sessionStorage.setItem(RESERVATION_RESULT_KEY, JSON.stringify(value))
}

export function clearReservationResult() {
  sessionStorage.removeItem(RESERVATION_RESULT_KEY)
}

function readJson(key) {
  const value = localStorage.getItem(key)
  if (!value) {
    return null
  }
  try {
    return JSON.parse(value)
  } catch {
    localStorage.removeItem(key)
    return null
  }
}

function readSessionJson(key) {
  const value = sessionStorage.getItem(key)
  if (!value) {
    return null
  }
  try {
    return JSON.parse(value)
  } catch {
    sessionStorage.removeItem(key)
    return null
  }
}

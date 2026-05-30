export function isValidIdCard(value) {
  return /^[0-9]{17}[0-9Xx]$/.test(value || '')
}

export function isValidPhone(value) {
  return /^1[3-9][0-9]{9}$/.test(value || '')
}

export function isPositiveNumber(value) {
  return Number(value) > 0
}

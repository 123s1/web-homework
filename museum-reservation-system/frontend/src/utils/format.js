export function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

export function formatDate(value) {
  return value || '-'
}

export function formatTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).slice(0, 5)
}

export function formatStatus(value) {
  const statusMap = {
    SUCCESS: '预约成功',
    CANCELLED: '已取消',
    DRAFT: '草稿',
    OPEN: '启用',
    CLOSED: '已关闭',
    DISABLED: '禁用'
  }
  return statusMap[value] || value || '-'
}

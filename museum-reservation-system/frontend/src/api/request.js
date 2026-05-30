import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

request.interceptors.response.use(
  (response) => {
    const contentType = response.headers?.['content-type'] || ''
    if (contentType.includes('text/csv')) {
      return response
    }
    const result = response.data
    if (result && Object.prototype.hasOwnProperty.call(result, 'success')) {
      if (result.success) {
        return result.data
      }
      return Promise.reject(new Error(result.message || '操作失败'))
    }
    return result
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络异常，请稍后重试'
    return Promise.reject(new Error(message))
  }
)

export default request

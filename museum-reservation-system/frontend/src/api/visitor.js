import request from './request'

export function loginVisitor(data) {
  return request.post('/visitor/login', data)
}

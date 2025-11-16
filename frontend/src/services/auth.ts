import http from './http'

interface ApiResponse<T> {
  success: boolean
  code: number
  message: string
  data: T
}

export const sendEmailCode = (email: string, scene: 'REGISTER' | 'LOGIN') => {
  return http.post<ApiResponse<boolean>>('/auth/code', { email, scene })
}



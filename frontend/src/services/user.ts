import http from './http'

interface ApiResponse<T> {
  success: boolean
  code: number
  message: string
  data: T
}

export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<ApiResponse<{ url: string; fileName: string }>>('/users/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}



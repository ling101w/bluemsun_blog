import http from './http'
import { API_BASE_URL } from '@/config'

export interface AttachmentSummary {
  id: number
  name: string
  url: string
  restrict: boolean
  size?: number
}

interface ApiResponse<T> {
  success: boolean
  code: number
  message: string
  data: T
}

export const uploadAttachments = (articleId: string | number, files: File[], accessScope: 'PUBLIC' | 'PAID_ONLY') => {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return http.post<ApiResponse<AttachmentSummary[]>, ApiResponse<AttachmentSummary[]>>(
    `/articles/${String(articleId)}/attachments`,
    formData,
    {
      params: { accessScope }
    }
  )
}

export const downloadAttachment = async (attachmentId: string | number) => {
  const base = API_BASE_URL.endsWith('/') ? API_BASE_URL.slice(0, -1) : API_BASE_URL
  const url = `${base}/attachments/${String(attachmentId)}`
  const token = localStorage.getItem('access_token')

  const response = await fetch(url, {
    headers: token
      ? {
          Authorization: `Bearer ${token}`,
          satoken: `Bearer ${token}`
        }
      : undefined
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || '附件下载失败')
  }

  const disposition = response.headers.get('content-disposition') ?? ''
  const matches = /filename\*=UTF-8''([^;]+)/i.exec(disposition)
  let fileName = 'attachment'
  if (matches && matches[1]) {
    fileName = decodeURIComponent(matches[1])
  }

  const blob = await response.blob()
  const objectUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(objectUrl)
}



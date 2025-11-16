import http from './http'

interface ApiResponse<T> {
  success: boolean
  code: number
  message: string
  data: T
}

export interface AiHistoryItem {
  id: number
  scene: string
  prompt: string
  response: string
  tokenUsage?: number
  createTime?: string
}

export const runAiTask = (payload: { scene: string; content: string; extra?: string }) => {
  return http.post<ApiResponse<AiHistoryItem>>('/ai/assistant', payload)
}

export const fetchAiHistory = (limit = 10) => {
  return http.get<ApiResponse<AiHistoryItem[]>>('/ai/assistant/history', { params: { limit } })
}



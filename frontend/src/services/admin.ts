import http from './http'
import type { OrderItem } from './order'
import type { UserProfile } from '@/stores/user'
import type { ArticleListItem } from './article'

interface ApiResponse<T> {
  success: boolean
  code: number
  message: string
  data: T
}

export const fetchAllOrders = (pageNo = 1, pageSize = 10) => {
  return http.get<ApiResponse<{ records: OrderItem[]; total: number; size: number; current: number }>>('/orders', {
    params: { pageNum: pageNo, pageSize }
  })
}

export const fetchAdminOverview = () => {
  return http.get<ApiResponse<{ userCount: number; articleCount: number; commentCount: number; orderCount: number }>>(
    '/admin/stats/overview'
  )
}

export const fetchAdminUsers = (params: { pageNo?: number; pageSize?: number; keyword?: string; status?: string }) => {
  return http.get<
    ApiResponse<{
      records: Array<UserProfile & { createTime: string }>
      total: number
      size: number
      current: number
    }>
  >('/admin/users', {
    params
  })
}

export const updateUserStatus = (id: number, status: string) => {
  return http.post<ApiResponse<boolean>>(`/admin/users/${id}/status`, null, { params: { status } })
}

export const rechargeUserBalance = (id: number, amount: number) => {
  return http.post<ApiResponse<boolean>>(`/admin/users/${id}/recharge`, null, {
    params: { amount }
  })
}

export const fetchAdminArticles = (params: {
  pageNo?: number
  pageSize?: number
  keyword?: string
  status?: string
  type?: string
}) => {
  return http.get<ApiResponse<{ records: ArticleListItem[]; total: number; size: number; current: number }>>(
    '/admin/articles',
    { params }
  )
}

export const publishArticle = (id: number) => {
  return http.post<ApiResponse<void>>(`/admin/articles/${id}/publish`)
}

export const offlineArticle = (id: number) => {
  return http.post<ApiResponse<void>>(`/admin/articles/${id}/offline`)
}

export const pinArticle = (id: number) => {
  return http.post<ApiResponse<void>>(`/admin/articles/${id}/pin`)
}

export const unpinArticle = (id: number) => {
  return http.post<ApiResponse<void>>(`/admin/articles/${id}/unpin`)
}


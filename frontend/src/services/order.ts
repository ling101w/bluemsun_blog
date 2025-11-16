import http from './http'

export interface OrderItem {
  id: number
  orderNo: string
  articleId: number
  amount: number
  status: string
  payChannel?: string
  payTime?: string
  createTime: string
  expireTime?: string
}

export interface OrderPaymentInfo {
  orderNo: string
  amount: number
  status: string
  payChannel?: string
  payUrl: string
  expireTime?: string
}

interface ApiResponse<T> {
  success: boolean
  code: number
  message: string
  data: T
}

export const createOrder = (payload: { articleId: string | number; amount: number; payChannel?: string }) => {
  return http.post<ApiResponse<OrderItem>>('/orders', payload)
}

export const fetchMyOrders = (pageNo = 1, pageSize = 10) => {
  return http.get<ApiResponse<{ records: OrderItem[]; total: number; size: number; current: number }>>('/orders/my', {
    params: { pageNum: pageNo, pageSize }
  })
}

export const fetchAllOrders = (pageNo = 1, pageSize = 10) => {
  return http.get<ApiResponse<{ records: OrderItem[]; total: number; size: number; current: number }>>('/orders', {
    params: { pageNum: pageNo, pageSize }
  })
}

export const createPayment = (orderNo: string, payload: { payChannel: string }) => {
  return http.post<ApiResponse<OrderPaymentInfo>>(`/orders/${orderNo}/pay`, payload)
}

export const mockPay = (orderNo: string) => {
  return http.post<ApiResponse<void>>(`/orders/${orderNo}/mock-pay`)
}

export const payOrderByBalance = (orderNo: string) => {
  return http.post<ApiResponse<void>>(`/orders/${orderNo}/pay/balance`)
}


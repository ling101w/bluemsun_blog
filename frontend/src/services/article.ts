import http from './http'

export interface ArticleListItem {
  id: string | number
  title: string
  summary: string
  coverUrl?: string
  type: string
  price?: number
  status: string
  publishTime?: string
  viewCount?: number
  likeCount?: number
  pinned?: boolean
  pinnedTime?: string
}

export interface ArticlePageResponse {
  records: ArticleListItem[]
  total: number
  size: number
  current: number
}

export interface ArticleDetailResponse extends ArticleListItem {
  authorId: string | number
  allowComment: boolean
  contentMd?: string
  contentHtml?: string
  attachments?: string
  commentCount?: number
  locked: boolean
  purchased: boolean
  canManageAttachments: boolean
  owner: boolean
}

export interface HotCreator {
  id: string | number
  nickname: string
  avatar?: string
  articleCount: number
  viewCount: number
  likeCount: number
}

export interface ArticleMutatePayload {
  title: string
  summary?: string
  coverUrl?: string
  categoryId?: number
  type: string
  price: number
  allowComment: boolean
  contentMd: string
  contentHtml?: string
  attachments?: string | null
}

export const fetchArticles = (params: {
  pageNo?: number
  pageSize?: number
  keyword?: string
  type?: string
  status?: string
  categoryId?: number
}) => {
  return http.get<ApiResponse<ArticlePageResponse>>('/articles', { params })
}

export const fetchArticleDetail = (id: string | number) => {
  return http.get<ApiResponse<ArticleDetailResponse>>(`/articles/${String(id)}`)
}

export const likeArticle = (id: string | number) => {
  return http.post<ApiResponse<void>>(`/articles/${String(id)}/like`)
}

export const trackArticleView = (id: string | number) => {
  return http.post<ApiResponse<void>>(`/articles/${String(id)}/view`)
}

export const fetchHotArticles = (limit = 10) => {
  return http.get<ApiResponse<ArticleListItem[]>>('/articles/hot', { params: { limit } })
}

export const fetchHotCreators = (limit = 5) => {
  return http.get<ApiResponse<HotCreator[]>>('/articles/creators/hot', { params: { limit } })
}

export const fetchAnnouncement = () => {
  return http.get<ApiResponse<ArticleDetailResponse | null>>('/articles/announcement')
}

export const createArticle = (payload: ArticleMutatePayload) => {
  return http.post<ApiResponse<string | number>>('/articles', payload)
}

export const updateArticle = (id: string | number, payload: ArticleMutatePayload) => {
  return http.put<ApiResponse<void>>(`/articles/${String(id)}`, payload)
}

export const deleteArticle = (id: string | number) => {
  return http.delete<ApiResponse<void>>(`/articles/${String(id)}`)
}

interface ApiResponse<T> {
  success: boolean
  message: string
  code: number
  data: T
}


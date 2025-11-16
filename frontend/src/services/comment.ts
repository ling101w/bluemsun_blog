import http from './http'

export interface CommentNode {
  // 后端 Long 通过 Jackson 转为字符串，这里兼容 string | number
  id: string | number
  userId: string | number
  nickname?: string
  avatar?: string
  content: string
  createTime: string
  likeCount: number
  children: CommentNode[]
}

interface ApiResponse<T> {
  success: boolean
  message: string
  code: number
  data: T
}

export const fetchComments = (articleId: string | number) => {
  // 读取评论树（仅返回已通过的评论）
  return http.get<ApiResponse<CommentNode[]>>('/comments', { params: { articleId: String(articleId) } })
}

export const submitComment = (payload: { articleId: string | number; content: string; parentId?: string | number }) => {
  // 切记：Long 可能超过 JS 安全整数范围，绝对不能转 Number！
  // 以字符串发送，后端 Jackson 可自动将字符串转换为 Long。
  const body = {
    articleId: String(payload.articleId),
    content: payload.content,
    parentId: payload.parentId !== undefined ? String(payload.parentId) : undefined
  }
  return http.post<ApiResponse<number>>('/comments', body)
}

export const likeComment = (id: string | number) => {
  // 点赞评论（登录后）
  return http.post<ApiResponse<void>>(`/comments/${String(id)}/like`)
}

export const replyComment = (parentId: string | number, payload: { articleId: string | number; content: string }) => {
  const body = {
    articleId: String(payload.articleId),
    content: payload.content
  }
  return http.post<ApiResponse<number>>(`/comments/${String(parentId)}/reply`, body)
}

export const previewEmoji = (payload: { content: string }) => {
  // Preview emoji placeholders (e.g. :smile:) before submitting
  return http.put<ApiResponse<string>>('/comments/emoji/preview', payload)
}


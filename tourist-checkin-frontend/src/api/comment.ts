import request from './request'
import type { CommentItem, CommentCreateForm, ApiResponse, PageResult } from '@/types'

export const getComments = (checkInId: number, page = 1, size = 20): Promise<ApiResponse<PageResult<CommentItem>>> =>
  request.get(`/comment/page`, { params: { checkInId, page, size } })

export const createComment = (data: CommentCreateForm): Promise<ApiResponse<CommentItem>> =>
  request.post('/comment', data)

export const likeComment = (commentId: number): Promise<ApiResponse<void>> =>
  request.post(`/comment/${commentId}/like`)

export const deleteComment = (commentId: number): Promise<ApiResponse<void>> =>
  request.delete(`/comment/${commentId}`)

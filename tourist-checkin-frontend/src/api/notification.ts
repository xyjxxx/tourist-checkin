import request from './request'
import type { NotificationItem, ApiResponse, PageResult } from '@/types'

export const getNotifications = (page = 1, size = 20): Promise<ApiResponse<PageResult<NotificationItem>>> =>
  request.get('/notification/page', { params: { page, size } })

export const markRead = (id: number): Promise<ApiResponse<void>> =>
  request.put(`/notification/read/${id}`)

export const markAllRead = (): Promise<ApiResponse<void>> =>
  request.put('/notification/read-all')

export const getUnreadCount = (): Promise<ApiResponse<number>> =>
  request.get('/notification/unread-count')

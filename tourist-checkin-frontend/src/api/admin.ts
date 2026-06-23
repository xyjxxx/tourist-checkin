import request from './request'
import type { ApiResponse, AdminOverview, PageResult, TravelNote, TopicItem, AchievementItem, MerchantPosition, Location, CommentItem, NotificationItem } from '@/types'

// 统计概览
export const getAdminOverview = (): Promise<ApiResponse<AdminOverview>> => request.get('/statistics/admin/overview')

// 游记管理
export const getAdminTravelNotes = (params: { page?: number; size?: number; status?: number; keyword?: string }): Promise<ApiResponse<PageResult<TravelNote>>> =>
  request.get('/travel-note/admin/list', { params })
export const adminAuditTravelNote = (id: number, status: number): Promise<ApiResponse<void>> =>
  request.put(`/travel-note/admin/${id}/audit`, null, { params: { status } })
export const adminDeleteTravelNote = (id: number): Promise<ApiResponse<void>> => request.delete(`/travel-note/admin/${id}`)
export const adminTogglePinTravelNote = (id: number): Promise<ApiResponse<void>> => request.put(`/travel-note/admin/${id}/pin`)

// 行程管理
export const getAdminTripPlans = (params: { page?: number; size?: number; keyword?: string }): Promise<ApiResponse<PageResult<import('@/types').TripPlanBrief>>> =>
  request.get('/trip-plan/admin/list', { params })
export const adminDeleteTripPlan = (id: number): Promise<ApiResponse<void>> => request.delete(`/trip-plan/admin/${id}`)

// 评论管理
export const getAdminComments = (params: { page?: number; size?: number }): Promise<ApiResponse<PageResult<CommentItem>>> =>
  request.get('/comment/admin/list', { params })
export const adminDeleteComment = (id: number): Promise<ApiResponse<void>> => request.delete(`/comment/admin/${id}`)

// 话题管理
export const getAdminTopics = (params: { page?: number; size?: number; keyword?: string }): Promise<ApiResponse<PageResult<TopicItem>>> =>
  request.get('/topic/admin/list', { params })
export const createAdminTopic = (data: { name: string; icon?: string; description?: string }): Promise<ApiResponse<TopicItem>> =>
  request.post('/topic/admin', data)
export const updateAdminTopic = (id: number, data: { name?: string; icon?: string; description?: string; isHot?: number }): Promise<ApiResponse<void>> =>
  request.put(`/topic/admin/${id}`, data)
export const deleteAdminTopic = (id: number): Promise<ApiResponse<void>> => request.delete(`/topic/admin/${id}`)

// 成就管理
export const getAdminAchievements = (): Promise<ApiResponse<AchievementItem[]>> => request.get('/achievement/admin/list')
export const createAdminAchievement = (data: Partial<import('@/types').AchievementItem>): Promise<ApiResponse<AchievementItem>> => request.post('/achievement/admin', data)
export const updateAdminAchievement = (id: number, data: Partial<import('@/types').AchievementItem>): Promise<ApiResponse<void>> => request.put(`/achievement/admin/${id}`, data)
export const deleteAdminAchievement = (id: number): Promise<ApiResponse<void>> => request.delete(`/achievement/admin/${id}`)

// 商户管理
export const getAdminMerchants = (params: { page?: number; size?: number; keyword?: string; category?: string }): Promise<ApiResponse<PageResult<MerchantPosition>>> =>
  request.get('/merchant/admin/list', { params })
export const createAdminMerchant = (data: Partial<import('@/types').MerchantPosition>): Promise<ApiResponse<MerchantPosition>> => request.post('/merchant/admin', data)
export const updateAdminMerchant = (id: number, data: Partial<import('@/types').MerchantPosition>): Promise<ApiResponse<void>> => request.put(`/merchant/admin/${id}`, data)
export const deleteAdminMerchant = (id: number): Promise<ApiResponse<void>> => request.delete(`/merchant/admin/${id}`)

// 地点管理
export const createAdminLocation = (data: Partial<import('@/types').Location>): Promise<ApiResponse<Location>> => request.post('/location/admin', data)
export const updateAdminLocation = (id: number, data: Partial<import('@/types').Location>): Promise<ApiResponse<void>> => request.put(`/location/admin/${id}`, data)
export const deleteAdminLocation = (id: number): Promise<ApiResponse<void>> => request.delete(`/location/admin/${id}`)

// 通知管理
export const getAdminNotifications = (params: { page?: number; size?: number }): Promise<ApiResponse<PageResult<NotificationItem>>> =>
  request.get('/notification/admin/list', { params })
export const adminSendNotification = (targetUserId: number, content: string): Promise<ApiResponse<void>> =>
  request.post('/notification/admin/send', { targetUserId, content })
export const adminBroadcastNotification = (content: string): Promise<ApiResponse<void>> =>
  request.post('/notification/admin/broadcast', { content })

// 举报管理
export const getAdminReports = (params: { page?: number; size?: number; status?: number }): Promise<ApiResponse<PageResult<unknown>>> =>
  request.get('/admin/reports', { params })
export const adminHandleReport = (id: number, data: { status: number; handleResult: string }): Promise<ApiResponse<void>> =>
  request.put(`/admin/report/${id}/handle`, data)

// 图片审核
export const getAdminImageAudits = (params: { page?: number; size?: number; status?: number }): Promise<ApiResponse<PageResult<unknown>>> =>
  request.get('/admin/image-audits', { params })
export const adminAuditImage = (id: number, data: { auditStatus: number; auditResult?: string }): Promise<ApiResponse<void>> =>
  request.put(`/admin/image-audit/${id}`, data)

// 敏感词管理
export const getAdminSensitiveWords = (params: { page?: number; size?: number }): Promise<ApiResponse<PageResult<unknown>>> =>
  request.get('/admin/sensitive-words', { params })
export const addAdminSensitiveWord = (data: { word: string; category?: string; level?: number }): Promise<ApiResponse<unknown>> =>
  request.post('/admin/sensitive-word', data)
export const deleteAdminSensitiveWord = (id: number): Promise<ApiResponse<void>> => request.delete(`/admin/sensitive-word/${id}`)

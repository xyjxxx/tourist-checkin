import request from './request'
import type { CheckIn, CheckInForm, ApiResponse } from '@/types'

export const getMyCheckIns = (): Promise<ApiResponse<CheckIn[]>> => {
  return request.get('/checkin/my')
}

export const getUserCheckIns = (userId: number): Promise<ApiResponse<CheckIn[]>> => {
  return request.get(`/checkin/user/${userId}`)
}

export const getRecentCheckIns = (): Promise<ApiResponse<CheckIn[]>> => {
  return request.get('/checkin/recent')
}

export const getLikedCheckIns = (): Promise<ApiResponse<CheckIn[]>> => {
  return request.get('/checkin/liked')
}

export const createCheckIn = (data: CheckInForm): Promise<ApiResponse<number>> => {
  return request.post('/checkin', data)
}

export const likeCheckIn = (checkInId: number): Promise<ApiResponse<void>> => {
  return request.post(`/checkin/${checkInId}/like`)
}

export const deleteCheckIn = (checkInId: number): Promise<ApiResponse<void>> => {
  return request.delete(`/checkin/${checkInId}`)
}

// ==================== 管理员接口 ====================

export const getAllCheckIns = (): Promise<ApiResponse<CheckIn[]>> => {
  return request.get('/checkin/admin/all')
}

export const getCheckInPage = (page: number, size: number, keyword?: string): Promise<ApiResponse<{ list: CheckIn[], total: number }>> => {
  return request.get('/checkin/admin/page', { params: { page, size, keyword } })
}

export const adminDeleteCheckIn = (checkInId: number): Promise<ApiResponse<void>> => {
  return request.delete(`/checkin/admin/${checkInId}`)
}

export const getCheckInStats = (): Promise<ApiResponse<{
  totalCheckIns: number
  todayCheckIns: number
  totalLikes: number
  totalUsers: number
  totalLocations: number
}>> => {
  return request.get('/checkin/admin/stats')
}

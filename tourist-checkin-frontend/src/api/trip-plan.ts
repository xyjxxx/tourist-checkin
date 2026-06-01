import request from './request'
import type { ApiResponse, TripPlan, TripPlanBrief } from '@/types'

export const createTripPlan = (data: {
  title: string; description?: string; city?: string; coverImage?: string
  startDate?: string; endDate?: string; isPublic?: boolean; days?: any[]
}): Promise<ApiResponse<TripPlan>> =>
  request.post('/trip-plan', data)

export const getTripPlanDetail = (id: number): Promise<ApiResponse<TripPlan>> =>
  request.get(`/trip-plan/${id}`)

export const getMyTripPlans = (): Promise<ApiResponse<TripPlanBrief[]>> =>
  request.get('/trip-plan/my')

export const getPublicTripPlans = (page = 1, size = 10): Promise<ApiResponse<TripPlanBrief[]>> =>
  request.get('/trip-plan/public', { params: { page, size } })

export const deleteTripPlan = (id: number): Promise<ApiResponse<void>> =>
  request.delete(`/trip-plan/${id}`)

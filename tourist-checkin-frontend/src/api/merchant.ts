import request from './request'
import type { ApiResponse, MerchantPosition } from '@/types'

export const getNearbyMerchants = (
  lat: number, lng: number, radius = 5000, category?: string, page = 1, size = 10
): Promise<ApiResponse<MerchantPosition[]>> =>
  request.get('/merchant/nearby', { params: { lat, lng, radius, category, page, size } })

export const getMerchantsByCategory = (
  category?: string, page = 1, size = 10
): Promise<ApiResponse<MerchantPosition[]>> =>
  request.get('/merchant/category', { params: { category, page, size } })

export const getMerchantDetail = (id: number): Promise<ApiResponse<MerchantPosition>> =>
  request.get(`/merchant/${id}`)

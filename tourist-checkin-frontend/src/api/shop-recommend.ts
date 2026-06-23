import request from './request'
import type { ApiResponse, PageResult } from '@/types'

export interface ShopRecommendItem {
  id: number
  shopName: string
  shopId: number
  userId: number
  nickname: string
  category: string
  city: string
  address: string
  reason: string
  images: string[]
  latitude: number
  longitude: number
  status: number
  isFeatured: boolean
  auditReason: string
  createdAt: string
  updatedAt: string
}

export interface ShopRecommendStats {
  pendingCount: number
  weeklyCount: number
  featuredCount: number
}

export function getShopRecommendAdminList(params: { page?: number; size?: number; status?: number; keyword?: string }): Promise<ApiResponse<PageResult<ShopRecommendItem>>> {
  return request.get('/shop-recommend/admin/list', { params })
}

export function adminAuditShopRecommend(id: number, data: { status: number; reason?: string }): Promise<ApiResponse<void>> {
  return request.put(`/shop-recommend/admin/${id}/audit`, data)
}

export function adminFeatureShopRecommend(id: number): Promise<ApiResponse<void>> {
  return request.put(`/shop-recommend/admin/${id}/feature`)
}

export function getShopRecommendAdminStats(): Promise<ApiResponse<ShopRecommendStats>> {
  return request.get('/shop-recommend/admin/stats')
}

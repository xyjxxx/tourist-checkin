import request from './request'
import type { FollowUser, ApiResponse, PageResult } from '@/types'

export const follow = (userId: number): Promise<ApiResponse<void>> =>
  request.post(`/follow/${userId}`)

export const unfollow = (userId: number): Promise<ApiResponse<void>> =>
  request.delete(`/follow/${userId}`)

export const checkFollow = (userId: number): Promise<ApiResponse<boolean>> =>
  request.get(`/follow/check/${userId}`)

export const getFollowers = (userId: number, page = 1, size = 20): Promise<ApiResponse<PageResult<FollowUser>>> =>
  request.get(`/follow/followers`, { params: { userId, page, size } })

export const getFollowing = (userId: number, page = 1, size = 20): Promise<ApiResponse<PageResult<FollowUser>>> =>
  request.get(`/follow/following`, { params: { userId, page, size } })

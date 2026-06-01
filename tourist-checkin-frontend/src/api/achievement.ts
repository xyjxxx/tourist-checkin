import request from './request'
import type { ApiResponse, AchievementItem } from '@/types'

export const getMyAchievements = (): Promise<ApiResponse<AchievementItem[]>> =>
  request.get('/achievement/my')

export const getAchievementDefinitions = (): Promise<ApiResponse<AchievementItem[]>> =>
  request.get('/achievement/definitions')

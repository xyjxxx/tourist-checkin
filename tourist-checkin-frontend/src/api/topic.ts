import request from './request'
import type { ApiResponse, TopicItem } from '@/types'

export const getTrendingTopics = (): Promise<ApiResponse<TopicItem[]>> =>
  request.get('/topic/trending')

export const searchTopics = (keyword: string): Promise<ApiResponse<TopicItem[]>> =>
  request.get('/topic/search', { params: { keyword } })

export const attachTopics = (checkInId: number, topicIds: number[]): Promise<ApiResponse<void>> =>
  request.post(`/topic/attach/${checkInId}`, topicIds)

import request from './request'
import type { ApiResponse, UserPoints, PointRecordItem } from '@/types'

export const getMyPoints = (): Promise<ApiResponse<UserPoints>> =>
  request.get('/point/my')

export const getPointRecords = (page = 1, size = 20): Promise<ApiResponse<PointRecordItem[]>> =>
  request.get('/point/records', { params: { page, size } })

import request from './request'
import type { ApiResponse, UserPoints, PointRecordItem, PageResult } from '@/types'

export const getMyPoints = (): Promise<ApiResponse<UserPoints>> =>
  request.get('/point/my')

export const getPointRecords = (page = 1, size = 20): Promise<ApiResponse<PageResult<PointRecordItem>>> =>
  request.get('/point/records', { params: { page, size } })

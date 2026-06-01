import request from './request'
import type { ApiResponse, UserReport, ChartData } from '@/types'

export const getUserReport = (year = 2026): Promise<ApiResponse<UserReport>> =>
  request.get('/statistics/report', { params: { year } })

export const getMonthlyTrend = (year = 2026): Promise<ApiResponse<ChartData>> =>
  request.get('/statistics/trend', { params: { year } })

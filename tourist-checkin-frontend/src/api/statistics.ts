import request from './request'
import type { ApiResponse, UserReport, ChartData } from '@/types'

export const getUserReport = (year = new Date().getFullYear()): Promise<ApiResponse<UserReport>> =>
  request.get('/statistics/report', { params: { year } })

export const getMonthlyTrend = (year = new Date().getFullYear()): Promise<ApiResponse<ChartData>> =>
  request.get('/statistics/trend', { params: { year } })

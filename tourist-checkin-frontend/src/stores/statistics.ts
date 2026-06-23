import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserReport, getMonthlyTrend } from '@/api/statistics'
import type { UserReport, ChartData } from '@/types'

export const useStatisticsStore = defineStore('statistics', () => {
  const report = ref<UserReport | null>(null)
  const trend = ref<ChartData | null>(null)
  const reportError = ref<string | null>(null)
  const trendError = ref<string | null>(null)

  // 兼容旧的 error getter
  const error = ref<string | null>(null)

  const fetchReport = async (year = new Date().getFullYear()) => {
    try {
      reportError.value = null
      const res = await getUserReport(year)
      report.value = res.data
    } catch (e: unknown) {
      reportError.value = (e as Error)?.message || '加载统计数据失败'
      error.value = reportError.value
    }
  }

  const fetchTrend = async (year = new Date().getFullYear()) => {
    try {
      trendError.value = null
      const res = await getMonthlyTrend(year)
      trend.value = res.data
    } catch (e: unknown) {
      trendError.value = (e as Error)?.message || '加载趋势数据失败'
      error.value = trendError.value
    }
  }

  return { report, trend, error, reportError, trendError, fetchReport, fetchTrend }
})

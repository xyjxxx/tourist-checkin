import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserReport, getMonthlyTrend } from '@/api/statistics'
import type { UserReport, ChartData } from '@/types'

export const useStatisticsStore = defineStore('statistics', () => {
  const report = ref<UserReport | null>(null)
  const trend = ref<ChartData | null>(null)

  const fetchReport = async (year = 2026) => {
    try {
      const res = await getUserReport(year)
      report.value = res.data
    } catch { /* silently fail */ }
  }

  const fetchTrend = async (year = 2026) => {
    try {
      const res = await getMonthlyTrend(year)
      trend.value = res.data
    } catch { /* silently fail */ }
  }

  return { report, trend, fetchReport, fetchTrend }
})

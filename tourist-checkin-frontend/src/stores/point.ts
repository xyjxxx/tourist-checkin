import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMyPoints, getPointRecords } from '@/api/point'
import type { UserPoints, PointRecordItem } from '@/types'

export const usePointStore = defineStore('point', () => {
  const myPoints = ref<UserPoints | null>(null)
  const records = ref<PointRecordItem[]>([])

  const fetchMyPoints = async () => {
    try {
      const res = await getMyPoints()
      myPoints.value = res.data
    } catch { /* silently fail */ }
  }

  const fetchRecords = async (page = 1, size = 20) => {
    try {
      const res = await getPointRecords(page, size)
      records.value = res.data
    } catch { /* silently fail */ }
  }

  return { myPoints, records, fetchMyPoints, fetchRecords }
})

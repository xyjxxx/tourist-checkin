import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getMyCheckIns,
  createCheckIn,
  likeCheckIn,
  deleteCheckIn,
  getRecentCheckIns
} from '@/api/checkin'
import type { CheckIn, CheckInForm } from '@/types'

export const useCheckInStore = defineStore('checkin', () => {
  const checkIns = ref<CheckIn[]>([])
  const recentCheckIns = ref<CheckIn[]>([])
  const loading = ref(false)

  const fetchMyCheckIns = async () => {
    loading.value = true
    try {
      const res = await getMyCheckIns()
      checkIns.value = res.data
      return res
    } finally {
      loading.value = false
    }
  }

  const fetchRecentCheckIns = async () => {
    loading.value = true
    try {
      const res = await getRecentCheckIns()
      recentCheckIns.value = res.data
      return res
    } finally {
      loading.value = false
    }
  }

  const addCheckIn = async (data: CheckInForm) => {
    const res = await createCheckIn(data)
    await fetchMyCheckIns()
    return res
  }

  const toggleLike = async (checkInId: number) => {
    // 同时搜索个人打卡和最近打卡列表
    const checkIn = checkIns.value.find(c => c.id === checkInId)
      || recentCheckIns.value.find(c => c.id === checkInId)
    if (!checkIn) return

    const previousLiked = checkIn.hasLiked
    const previousCount = checkIn.likeCount

    // 乐观更新
    checkIn.hasLiked = !previousLiked
    checkIn.likeCount += checkIn.hasLiked ? 1 : -1

    try {
      await likeCheckIn(checkInId)
    } catch {
      // 请求失败时回滚
      checkIn.hasLiked = previousLiked
      checkIn.likeCount = previousCount
    }
  }

  const removeCheckIn = async (checkInId: number) => {
    await deleteCheckIn(checkInId)
    checkIns.value = checkIns.value.filter(c => c.id !== checkInId)
  }

  return {
    checkIns,
    recentCheckIns,
    loading,
    fetchMyCheckIns,
    fetchRecentCheckIns,
    addCheckIn,
    toggleLike,
    removeCheckIn
  }
})

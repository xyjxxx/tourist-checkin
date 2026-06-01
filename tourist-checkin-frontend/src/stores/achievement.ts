import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMyAchievements, getAchievementDefinitions } from '@/api/achievement'
import type { AchievementItem } from '@/types'

export const useAchievementStore = defineStore('achievement', () => {
  const myAchievements = ref<AchievementItem[]>([])
  const allDefinitions = ref<AchievementItem[]>([])
  const error = ref<string | null>(null)

  const fetchMyAchievements = async () => {
    try {
      error.value = null
      const res = await getMyAchievements()
      myAchievements.value = res.data
    } catch (e: any) {
      error.value = e?.message || '获取成就数据失败'
    }
  }

  const fetchDefinitions = async () => {
    try {
      error.value = null
      const res = await getAchievementDefinitions()
      allDefinitions.value = res.data
    } catch (e: any) {
      error.value = e?.message || '获取成就定义失败'
    }
  }

  const unlockedCount = () => myAchievements.value.filter(a => a.isUnlocked).length

  return {
    myAchievements, allDefinitions, error,
    fetchMyAchievements, fetchDefinitions, unlockedCount
  }
})

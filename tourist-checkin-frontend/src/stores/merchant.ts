import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getNearbyMerchants, getMerchantsByCategory } from '@/api/merchant'
import type { MerchantPosition } from '@/types'

export const useMerchantStore = defineStore('merchant', () => {
  const nearbyList = ref<MerchantPosition[]>([])
  const categoryList = ref<MerchantPosition[]>([])

  const fetchNearby = async (lat: number, lng: number, radius = 5000, category?: string) => {
    try {
      const res = await getNearbyMerchants(lat, lng, radius, category)
      nearbyList.value = res.data
    } catch { /* silently fail */ }
  }

  const fetchByCategory = async (category?: string, page = 1, size = 10) => {
    try {
      const res = await getMerchantsByCategory(category, page, size)
      categoryList.value = res.data
    } catch { /* silently fail */ }
  }

  return { nearbyList, categoryList, fetchNearby, fetchByCategory }
})

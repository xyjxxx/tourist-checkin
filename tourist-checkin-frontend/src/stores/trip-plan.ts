import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMyTripPlans, getPublicTripPlans, deleteTripPlan } from '@/api/trip-plan'
import type { TripPlan, TripPlanBrief } from '@/types'

export const useTripPlanStore = defineStore('trip-plan', () => {
  const myPlans = ref<TripPlanBrief[]>([])
  const publicPlans = ref<TripPlanBrief[]>([])
  const currentPlan = ref<TripPlan | null>(null)

  const fetchMyPlans = async () => {
    try {
      const res = await getMyTripPlans()
      myPlans.value = res.data
    } catch { /* silently fail */ }
  }

  const fetchPublicPlans = async (page = 1, size = 10) => {
    try {
      const res = await getPublicTripPlans(page, size)
      publicPlans.value = res.data
    } catch { /* silently fail */ }
  }

  const handleDelete = async (planId: number) => {
    await deleteTripPlan(planId)
    myPlans.value = myPlans.value.filter(p => p.id !== planId)
  }

  return {
    myPlans, publicPlans, currentPlan,
    fetchMyPlans, fetchPublicPlans, handleDelete
  }
})

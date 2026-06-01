<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-3xl mx-auto p-4">
      <div v-if="plan" class="card-elevated p-6">
        <h1 class="text-2xl font-heading font-bold text-[#18191C]">{{ plan.title }}</h1>
        <p v-if="plan.city" class="text-[#FB7299] mt-1 font-medium">{{ plan.city }}</p>
        <p v-if="plan.description" class="text-sm text-[#61666D] mt-2 leading-relaxed">{{ plan.description }}</p>

        <div class="flex items-center gap-2 mt-4 text-sm text-[#9499A0] bg-[#F1F2F3] rounded-lg p-3">
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>
          <span>{{ plan.startDate }} ~ {{ plan.endDate }}</span>
          <span class="text-[#E3E5E7]">·</span>
          <span class="font-semibold text-[#FB7299]">{{ plan.totalDays }}天</span>
        </div>

        <div v-if="plan.days?.length" class="mt-6 space-y-4">
          <div v-for="day in plan.days" :key="day.id" class="rounded-card border border-[#E3E5E7] p-5 bg-white shadow-sm">
            <h3 class="font-heading font-bold text-base text-[#18191C] mb-3 flex items-center gap-2">
              <span class="w-8 h-8 rounded-lg bg-[#FFD6E0] text-[#FB7299] flex items-center justify-center text-sm font-bold">
                {{ day.dayNumber }}
              </span>
              第{{ day.dayNumber }}天
              <span v-if="day.date" class="text-[#9499A0] text-sm font-normal ml-1">{{ day.date }}</span>
            </h3>
            <div v-if="day.pois?.length" class="space-y-2">
              <div
                v-for="poi in day.pois" :key="poi.id"
                class="flex items-center gap-3 p-3 rounded-xl bg-[#F1F2F3]/50 border border-[#F1F2F3]"
              >
                <span class="text-xs font-bold text-[#FB7299] w-7 h-7 rounded-full bg-[#FFD6E0] flex items-center justify-center flex-shrink-0">
                  {{ poi.sortOrder + 1 }}
                </span>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-[#18191C]">{{ poi.name }}</p>
                  <p v-if="poi.address" class="text-xs text-[#9499A0] truncate">{{ poi.address }}</p>
                </div>
                <span v-if="poi.durationMinutes" class="text-xs text-[#9499A0] bg-[#E3E5E7] px-2 py-1 rounded-full">
                  {{ poi.durationMinutes }}min
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-skeleton v-else :rows="8" animated class="card-elevated p-6" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getTripPlanDetail } from '@/api/trip-plan'
import type { TripPlan } from '@/types'

const route = useRoute()
const plan = ref<TripPlan | null>(null)

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const res = await getTripPlanDetail(id)
    plan.value = res.data as any
  } catch { /* ignore */ }
})
</script>

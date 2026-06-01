<template>
  <div v-if="points" class="points-display">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-amber-100 flex items-center justify-center">
          <svg class="w-5 h-5 text-amber-500" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
        </div>
        <div>
          <span class="text-2xl font-extrabold text-amber-500 font-heading">{{ points.currentPoints }}</span>
          <span class="text-sm text-[#9499A0] ml-1">积分</span>
        </div>
      </div>
      <el-tag :type="levelTagType" size="default" round>
        {{ points.levelName }}
      </el-tag>
    </div>
    <el-progress
      v-if="points.nextLevelPoints > 0"
      :percentage="Math.round((points.currentPoints / points.nextLevelPoints) * 100)"
      :stroke-width="8"
      class="mt-3"
      color="#F59E0B"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { usePointStore } from '@/stores/point'
import type { UserPoints } from '@/types'

const store = usePointStore()
const points = computed<UserPoints | null>(() => store.myPoints)

const levelTagType = computed(() => {
  const level = points.value?.level ?? 0
  if (level >= 4) return 'danger'
  if (level >= 3) return 'warning'
  if (level >= 2) return 'success'
  return 'info'
})

onMounted(() => { store.fetchMyPoints() })
</script>

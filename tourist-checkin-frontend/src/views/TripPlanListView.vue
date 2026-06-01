<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-5xl mx-auto p-4">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-heading font-bold text-[#18191C]">行程规划</h2>
        <el-button type="primary" @click="$router.push('/trip-plans/create')" round>
          <el-icon class="mr-1"><Plus /></el-icon>
          新建行程
        </el-button>
      </div>
      <el-tabs v-model="tab" class="mb-4">
        <el-tab-pane label="我的行程" name="my" />
        <el-tab-pane label="公开行程" name="public" />
      </el-tabs>

      <div v-if="plans.length" class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="plan in plans" :key="plan.id"
          class="card-elevated overflow-hidden cursor-pointer group"
          @click="$router.push(`/trip-plans/${plan.id}`)"
        >
          <el-image v-if="plan.coverImage" :src="plan.coverImage" fit="cover" class="w-full h-40 group-hover:scale-105 transition-transform duration-500" />
          <div v-else class="w-full h-32 bg-gradient-to-br from-[#FFD6E0] to-[#FFC1D3] flex items-center justify-center">
            <svg class="w-10 h-10 text-[#FFC1D3]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
            </svg>
          </div>
          <div class="p-4">
            <h4 class="font-heading font-semibold text-[#18191C]">{{ plan.title }}</h4>
            <p v-if="plan.city" class="text-xs text-[#9499A0] mt-1">{{ plan.city }}</p>
            <div class="flex items-center justify-between mt-3 pt-2 border-t border-[#F1F2F3] text-xs text-[#9499A0]">
              <span class="flex items-center gap-1">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>
                {{ plan.totalDays }}天
              </span>
              <span>{{ plan.createdAt }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无行程" :image-size="60" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { useTripPlanStore } from '@/stores/trip-plan'

const store = useTripPlanStore()
const tab = ref('my')

const plans = computed(() => tab.value === 'my' ? store.myPlans : store.publicPlans)

onMounted(() => { store.fetchMyPlans(); store.fetchPublicPlans() })
</script>

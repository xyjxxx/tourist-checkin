<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-2xl mx-auto p-4">
      <div class="rounded-xl overflow-hidden mb-6 bg-gradient-to-br from-amber-400 via-orange-400 to-amber-500 text-white p-6 shadow-lg">
        <div class="text-center">
          <div class="text-4xl font-extrabold">{{ store.myPoints?.currentPoints ?? 0 }}</div>
          <div class="text-sm opacity-80 mt-1">当前积分</div>
          <el-tag effect="plain" round class="mt-3 bg-white/20 text-white border-white/30">
            {{ store.myPoints?.levelName ?? '普通用户' }}
          </el-tag>
        </div>
      </div>

      <div v-if="store.records.length" class="space-y-2">
        <div
          v-for="item in store.records" :key="item.id"
          class="flex items-center justify-between p-4 bg-white rounded-xl border border-[#F1F2F3] transition-all hover:shadow-card-hover"
        >
          <div>
            <p class="text-sm font-medium text-[#18191C]">{{ item.description }}</p>
            <span class="text-xs text-[#9499A0]">{{ item.createdAt }}</span>
          </div>
          <span class="font-bold text-base" :class="item.points > 0 ? 'text-green-500' : 'text-red-500'">
            {{ item.points > 0 ? '+' : '' }}{{ item.points }}
          </span>
        </div>
      </div>
      <el-empty v-else description="暂无积分记录" :image-size="60" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { usePointStore } from '@/stores/point'

const store = usePointStore()
onMounted(() => { store.fetchMyPoints(); store.fetchRecords() })
</script>

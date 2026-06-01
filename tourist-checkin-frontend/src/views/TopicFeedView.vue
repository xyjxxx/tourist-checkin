<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-4xl mx-auto p-4">
      <h1 class="text-xl font-bold text-[#18191C] mb-4">#{{ topicName }}</h1>

      <div class="card-elevated p-5 mb-6">
        <TrendingTopics />
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="item in checkIns" :key="item.id"
          class="p-4 bg-white rounded-xl border border-[#F1F2F3] shadow-sm hover:shadow-card-hover transition-all"
        >
          <div class="flex items-center gap-2 mb-3">
            <el-avatar :size="32" :src="item.avatar" />
            <span class="text-sm font-semibold text-[#18191C]">{{ item.username }}</span>
            <span class="text-xs text-[#9499A0] ml-auto">{{ item.checkInTime }}</span>
          </div>
          <h4 class="font-semibold text-sm text-[#18191C]">{{ item.locationName }}</h4>
          <p v-if="item.content" class="text-sm text-[#61666D] mt-1 leading-relaxed">{{ item.content }}</p>
        </div>
      </div>
      <el-empty v-if="!checkIns.length" description="该话题暂无打卡" :image-size="60" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import TrendingTopics from '@/components/TrendingTopics.vue'
import type { CheckIn } from '@/types'

const route = useRoute()
const topicName = ref(route.params.name as string)
const checkIns = ref<CheckIn[]>([])
</script>

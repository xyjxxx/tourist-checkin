<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-4xl mx-auto p-4">
      <el-tabs v-model="tab" class="mb-4">
        <el-tab-pane label="已解锁" name="unlocked" />
        <el-tab-pane label="全部" name="all" />
      </el-tabs>

      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div
          v-for="item in filteredAchievements" :key="item.id"
          class="achievement-card p-4 rounded-xl text-center transition-all duration-200"
          :class="{
            'bg-gradient-to-br from-amber-50 to-yellow-50 border border-amber-200 shadow-sm': item.isUnlocked,
            'bg-white border border-gray-100 opacity-50 grayscale': !item.isUnlocked
          }"
        >
          <div class="text-3xl">{{ item.icon || '🏅' }}</div>
          <h4 class="font-semibold text-sm mt-2">{{ item.name }}</h4>
          <p class="text-xs text-[#9499A0] mt-1">{{ item.description }}</p>
          <el-progress
            v-if="!item.isUnlocked && item.progress !== undefined"
            :percentage="item.progress"
            :stroke-width="4"
            class="mt-2"
          />
          <div v-if="item.isUnlocked && item.unlockedAt" class="text-xs text-[#9499A0] mt-2">
            {{ item.unlockedAt }}
          </div>
          <el-tag v-if="item.isUnlocked" type="warning" size="small" round class="mt-2">
            +{{ item.pointsReward }} 积分
          </el-tag>
        </div>
      </div>
      <el-empty v-if="!filteredAchievements.length" description="暂无成就" :image-size="60" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAchievementStore } from '@/stores/achievement'

const store = useAchievementStore()
const tab = ref('unlocked')

const filteredAchievements = computed(() => {
  if (tab.value === 'unlocked') return store.myAchievements.filter(a => a.isUnlocked)
  return store.allDefinitions
})

onMounted(() => {
  store.fetchMyAchievements()
  store.fetchDefinitions()
})
</script>

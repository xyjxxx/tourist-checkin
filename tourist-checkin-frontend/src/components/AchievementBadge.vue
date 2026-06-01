<template>
  <div class="achievement-badge">
    <h3 class="text-base font-bold text-[#18191C] mb-3">
      成就徽章
      <span class="text-xs text-[#9499A0] font-normal ml-1">({{ unlockedCount }}/{{ achievements.length }})</span>
    </h3>
    <div class="flex flex-wrap gap-3">
      <el-tooltip
        v-for="item in achievements" :key="item.id"
        :content="`${item.name}: ${item.description}`"
        placement="top"
      >
        <div
          class="achievement-icon text-center cursor-pointer"
          :class="{ unlocked: item.isUnlocked, locked: !item.isUnlocked }"
        >
          <span class="text-2xl">{{ item.icon || '🏅' }}</span>
          <div class="text-xs mt-1 font-medium">{{ item.name }}</div>
        </div>
      </el-tooltip>
    </div>
    <el-empty v-if="!achievements.length" description="暂无成就" :image-size="40" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAchievementStore } from '@/stores/achievement'

const store = useAchievementStore()
const achievements = computed(() => store.myAchievements)
const unlockedCount = computed(() => store.unlockedCount())

onMounted(() => { store.fetchMyAchievements() })
</script>

<style scoped>
.achievement-icon {
  width: 68px;
  padding: 10px 8px;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.achievement-icon.unlocked {
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.15);
}

.achievement-icon.unlocked:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(245, 158, 11, 0.25);
}

.achievement-icon.locked {
  opacity: 0.35;
  filter: grayscale(1);
  background: #F1F5F9;
}
</style>

<template>
  <div class="travel-note-card cursor-pointer group" @click="$router.push(`/travel-notes/${note.id}`)">
    <div class="relative overflow-hidden aspect-[16/10]">
      <el-image
        v-if="note.coverImage"
        :src="note.coverImage"
        fit="cover"
        class="w-full h-full group-hover:scale-105 transition-transform duration-500"
      />
      <div v-else class="w-full h-full bg-gradient-to-br from-[#FFD6E0] to-[#FFC1D3] flex items-center justify-center">
        <svg class="w-10 h-10 text-[#FF85AB]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
        </svg>
      </div>
    </div>
    <div class="p-3">
      <h4 class="font-semibold text-sm text-[#18191C] line-clamp-2 leading-snug">{{ note.title }}</h4>
      <p class="text-xs text-[#9499A0] mt-1 line-clamp-1">{{ note.summary }}</p>
      <div class="flex items-center justify-between mt-3 pt-2 border-t border-[#F1F2F3]">
        <div class="flex items-center gap-1.5">
          <el-avatar :size="20" :src="note.author?.avatar" />
          <span class="text-xs text-[#61666D]">{{ note.author?.username }}</span>
        </div>
        <div class="flex items-center gap-3 text-xs text-[#9499A0]">
          <span>{{ note.viewCount }} 浏览</span>
          <span>{{ note.likeCount }} 赞</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TravelNote } from '@/types'
defineProps<{ note: TravelNote }>()
</script>

<style scoped>
.travel-note-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px 0 rgba(0,0,0,0.04), 0 1px 2px -1px rgba(0,0,0,0.04);
  border: 1px solid rgba(227,229,231,0.4);
  transition: all 0.2s ease;
}

.travel-note-card:hover {
  box-shadow: 0 4px 12px 0 rgba(0,0,0,0.08), 0 2px 4px -2px rgba(0,0,0,0.06);
  transform: translateY(-2px);
}
</style>

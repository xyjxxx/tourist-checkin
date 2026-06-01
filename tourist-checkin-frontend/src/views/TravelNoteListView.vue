<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-5xl mx-auto p-4">
      <div class="flex items-center justify-between mb-4">
        <h1 class="text-lg font-heading font-bold text-[#18191C]">游记</h1>
        <el-button type="primary" @click="$router.push('/travel-notes/create')" round>
          <el-icon class="mr-1"><Plus /></el-icon>
          写游记
        </el-button>
      </div>
      <el-tabs v-model="tab" class="mb-4">
        <el-tab-pane label="热门" name="hot" />
        <el-tab-pane label="最新" name="recent" />
      </el-tabs>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <TravelNoteCard v-for="note in notes" :key="note.id" :note="note" />
      </div>
      <el-empty v-if="!notes.length" description="暂无游记" :image-size="60" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { useTravelNoteStore } from '@/stores/travel-note'
import TravelNoteCard from '@/components/TravelNoteCard.vue'

const store = useTravelNoteStore()
const tab = ref('hot')

const notes = computed(() => tab.value === 'hot' ? store.hotNotes : store.recentNotes)

onMounted(() => { store.fetchHot(); store.fetchRecent() })
</script>

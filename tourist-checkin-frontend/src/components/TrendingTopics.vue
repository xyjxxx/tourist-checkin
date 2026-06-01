<template>
  <div class="trending-topics">
    <h3 class="text-base font-bold text-[#18191C] mb-3">热门话题</h3>
    <div class="flex flex-wrap gap-2">
      <el-tag
        v-for="topic in topics"
        :key="topic.id"
        :type="topic.isHot ? 'danger' : ''"
        :effect="topic.isHot ? 'dark' : 'plain'"
        round
        class="cursor-pointer hover:scale-105 transition-transform"
        @click="$router.push(`/topic/${topic.name}`)"
      >
        #{{ topic.name }}
        <span class="text-xs ml-1 opacity-75">({{ topic.checkInCount }})</span>
      </el-tag>
    </div>
    <el-empty v-if="!topics.length" description="暂无话题" :image-size="40" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getTrendingTopics } from '@/api/topic'
import type { TopicItem } from '@/types'

const topics = ref<TopicItem[]>([])

onMounted(async () => {
  try {
    const res = await getTrendingTopics()
    topics.value = (res.data as any) || []
  } catch {
    // 话题加载失败，静默处理，不影响页面
  }
})
</script>

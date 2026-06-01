<template>
  <div class="min-h-screen">
    <div class="max-w-3xl mx-auto p-4">
      <div v-if="note" class="card-elevated p-6">
        <h1 class="text-2xl font-heading font-bold text-[#18191C]">{{ note.title }}</h1>
        <div class="flex items-center gap-3 mt-3 text-sm text-[#9499A0]">
          <el-avatar :size="32" :src="note.author?.avatar" class="ring-2 ring-[#FFD6E0]" />
          <span class="font-medium text-[#18191C]">{{ note.author?.username }}</span>
          <span class="text-gray-300">·</span>
          <span>{{ note.createdAt }}</span>
          <span v-if="note.city" class="text-gray-300">·</span>
          <span v-if="note.city" class="text-[#FB7299]">{{ note.city }}</span>
        </div>

        <div class="flex gap-2 mt-4">
          <el-tag v-for="tag in note.tags" :key="tag" size="small" round class="bg-[#F1F2F3] text-[#FB7299] border-[#FFC1D3]">
            #{{ tag }}
          </el-tag>
        </div>

        <el-image v-if="note.coverImage" :src="note.coverImage" fit="cover" class="w-full rounded-xl mt-4" />

        <div class="mt-6 text-gray-700 leading-relaxed" v-html="sanitizeHtml(note.content)" />

        <div class="flex items-center gap-4 mt-6 pt-4 border-t border-[#E3E5E7]">
          <el-button :icon="note.hasLiked ? StarFilled : Star" @click="handleLike" round>
            {{ note.likeCount }} 赞
          </el-button>
          <el-button :icon="note.hasCollected ? FolderChecked : FolderAdd" @click="handleCollect" round>
            {{ note.collectCount }} 收藏
          </el-button>
        </div>
      </div>
      <el-skeleton v-else :rows="10" animated class="card-elevated p-6" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Star, StarFilled, FolderChecked, FolderAdd } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getTravelNoteDetail } from '@/api/travel-note'
import { useTravelNoteStore } from '@/stores/travel-note'
import type { TravelNote } from '@/types'

const sanitizeHtml = (html: string): string => {
  if (!html) return ''
  const div = document.createElement('div')
  div.innerHTML = html
  // 移除所有script、iframe、object、embed标签
  div.querySelectorAll('script,iframe,object,embed,form,input,textarea,button,select').forEach(el => el.remove())
  // 移除所有on*事件属性和javascript:协议
  div.querySelectorAll('*').forEach(el => {
    Array.from(el.attributes).forEach(attr => {
      if (attr.name.startsWith('on') || attr.value.trim().toLowerCase().startsWith('javascript:')) {
        el.removeAttribute(attr.name)
      }
    })
  })
  return div.innerHTML
}

const route = useRoute()
const store = useTravelNoteStore()
const note = ref<TravelNote | null>(null)

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const res = await getTravelNoteDetail(id)
    note.value = res.data as any
  } catch (error: any) {
    ElMessage.error(error?.message || '加载游记详情失败')
  }
})

const handleLike = async () => {
  if (!note.value) return
  await store.handleLike(note.value.id)
  note.value = { ...note.value, hasLiked: !note.value.hasLiked, likeCount: note.value.likeCount + (note.value.hasLiked ? -1 : 1) }
}

const handleCollect = async () => {
  if (!note.value) return
  await store.handleCollect(note.value.id)
  note.value = { ...note.value, hasCollected: !note.value.hasCollected, collectCount: note.value.collectCount + (note.value.hasCollected ? -1 : 1) }
}
</script>

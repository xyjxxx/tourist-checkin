<template>
  <div class="comment-item py-3">
    <div class="flex items-start gap-3">
      <el-avatar :size="36" :src="comment.user?.avatar" class="flex-shrink-0" />
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2 flex-wrap">
          <span class="text-sm font-semibold text-[#18191C]">{{ comment.user?.username }}</span>
          <span class="text-xs text-[#9499A0]">{{ formatDateSafe(comment.createdAt) }}</span>
        </div>
        <p class="text-sm text-[#61666D] mt-1.5 leading-relaxed">{{ comment.content }}</p>
        <div class="flex items-center gap-4 mt-2">
          <button
            class="flex items-center gap-1 text-xs text-[#9499A0] hover:text-[#FB7299] transition-colors cursor-pointer bg-transparent border-none"
            @click="toggleReply"
          >
            <el-icon :size="14"><ChatDotRound /></el-icon>
            <span v-if="comment.likeCount">{{ comment.likeCount }}</span>
            回复
          </button>
          <button
            v-if="comment.user?.id === currentUserId"
            class="flex items-center gap-1 text-xs text-[#9499A0] hover:text-red-500 transition-colors cursor-pointer bg-transparent border-none"
            @click="$emit('delete', comment.id)"
          >
            <el-icon :size="14"><Delete /></el-icon>
            删除
          </button>
        </div>

        <div v-if="showReply" class="mt-3 animate-fade-in">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="2"
            size="small"
            :placeholder="'回复 ' + comment.user?.username"
            class="mb-2"
          />
          <div class="flex gap-2">
            <el-button size="small" type="primary" :disabled="!replyContent.trim()" @click="doReply">
              回复
            </el-button>
            <el-button size="small" @click="showReply = false">取消</el-button>
          </div>
        </div>

        <div v-if="comment.replies?.length" class="mt-3 bg-[#F1F2F3]/50 rounded-lg p-3 border border-[#E3E5E7]/50">
          <CommentItem
            v-for="reply in comment.replies" :key="reply.id"
            :comment="reply" :current-user-id="currentUserId"
            @reply="(pid, content, ruid, rid) => $emit('reply', pid, content, ruid, rid)"
            @like="$emit('like', reply.id)"
            @delete="(id) => $emit('delete', id)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { CommentItem as CommentType } from '@/types'
import { formatDateSafe } from '@/utils/date'
import { ChatDotRound, Delete } from '@element-plus/icons-vue'

defineProps<{ comment: CommentType; currentUserId: number }>()
const emit = defineEmits<{
  reply: [parentId: number, content: string, replyToUserId?: number, replyToId?: number]
  like: [id: number]
  delete: [id: number]
}>()

const showReply = ref(false)
const replyContent = ref('')

const toggleReply = () => { showReply.value = !showReply.value }

const doReply = () => {
  if (!replyContent.value.trim()) return
  emit('reply', 0, replyContent.value.trim())
  replyContent.value = ''
  showReply.value = false
}
</script>

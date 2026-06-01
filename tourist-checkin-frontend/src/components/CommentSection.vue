<template>
  <div class="comment-section">
    <div class="comment-input p-4 bg-[#F1F2F3]/50 rounded-xl mb-4 border border-[#E3E5E7]">
      <el-input
        v-model="content"
        type="textarea"
        :rows="2"
        placeholder="写下你的评论..."
        maxlength="500"
        show-word-limit
        class="mb-3"
      />
      <div class="flex justify-end">
        <el-button type="primary" size="default" :disabled="!content.trim()" @click="submitComment" class="px-5">
          发表评论
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="comment-list min-h-[120px]">
      <div v-if="comments.length" class="space-y-1">
        <div v-for="item in comments" :key="item.id" class="comment-item px-1">
          <CommentItem
            :comment="item"
            :current-user-id="currentUserId"
            @reply="handleReply"
            @like="handleLike"
            @delete="handleDelete"
          />
        </div>
      </div>
      <el-empty v-else description="暂无评论，来写第一条吧" :image-size="60" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getComments, createComment, likeComment, deleteComment } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { CommentItem as CommentType } from '@/types'
import CommentItem from './CommentItem.vue'

const props = defineProps<{ checkInId: number; currentUserId: number }>()

const comments = ref<CommentType[]>([])
const content = ref('')
const loading = ref(false)

const loadComments = async () => {
  loading.value = true
  try {
    const res = await getComments(props.checkInId)
    comments.value = res.data as any
  } catch (error: any) {
    ElMessage.error(error?.message || '加载评论失败')
  } finally {
    loading.value = false
  }
}

const submitComment = async () => {
  if (!content.value.trim()) return
  try {
    await createComment({ checkInId: props.checkInId, content: content.value.trim() })
    content.value = ''
    ElMessage.success('评论成功')
    loadComments()
  } catch (error: any) {
    ElMessage.error(error?.message || '评论发表失败')
  }
}

const handleReply = async (parentId: number, replyContent: string, replyToUserId?: number, replyToId?: number) => {
  try {
    await createComment({
      checkInId: props.checkInId,
      content: replyContent,
      parentId,
      replyToUserId,
      replyToId: replyToId ?? parentId
    })
    ElMessage.success('回复成功')
    loadComments()
  } catch (error: any) {
    ElMessage.error(error?.message || '回复失败')
  }
}

const handleLike = async (commentId: number) => {
  await likeComment(commentId)
  loadComments()
}

const handleDelete = async (commentId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteComment(commentId)
    ElMessage.success('已删除')
    loadComments()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

onMounted(loadComments)
</script>

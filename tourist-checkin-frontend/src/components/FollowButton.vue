<template>
  <el-button
    :type="isFollowing ? 'default' : 'primary'"
    :size="size"
    :loading="loading"
    round
    class="follow-btn"
    @click="toggleFollow"
  >
    {{ isFollowing ? '已关注' : '+ 关注' }}
  </el-button>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { follow, unfollow, checkFollow } from '@/api/follow'
import { ElMessage } from 'element-plus'

const props = defineProps<{ userId: number; size?: 'small' | 'default' | 'large' }>()

const isFollowing = ref(false)
const loading = ref(false)

const checkFollowStatus = async () => {
  try {
    const res = await checkFollow(props.userId)
    isFollowing.value = res.data === true || (res.data as any)?.following === true
  } catch { /* 状态查询失败静默处理 */ }
}

const toggleFollow = async () => {
  loading.value = true
  try {
    if (isFollowing.value) {
      await unfollow(props.userId)
      isFollowing.value = false
      ElMessage.success('已取消关注')
    } else {
      await follow(props.userId)
      isFollowing.value = true
      ElMessage.success('关注成功')
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '操作失败')
  }
  finally { loading.value = false }
}

onMounted(checkFollowStatus)
</script>

<style scoped>
.follow-btn {
  background-color: #FB7299;
  border-color: #FB7299;
  color: white;
}

.follow-btn:hover {
  background-color: #E85D88;
  border-color: #E85D88;
  color: white;
}

.follow-btn:deep(.is-plain) {
  background-color: white;
  border-color: #E3E5E7;
  color: #9499A0;
}
</style>

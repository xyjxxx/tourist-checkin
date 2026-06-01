<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-2xl mx-auto p-4">
      <el-tabs v-model="tab" class="mb-4">
        <el-tab-pane label="关注" name="following" />
        <el-tab-pane label="粉丝" name="followers" />
      </el-tabs>

      <div v-if="users.length" class="space-y-2">
        <div
          v-for="u in users" :key="u.id"
          class="flex items-center justify-between p-4 bg-white rounded-xl border border-[#F1F2F3] shadow-sm hover:shadow-card-hover transition-all"
        >
          <div class="flex items-center gap-3 cursor-pointer" @click="$router.push(`/profile/${u.id}`)">
            <el-avatar :size="44" :src="u.avatar" class="ring-2 ring-[#FFD6E0]" />
            <div>
              <p class="text-sm font-semibold text-[#18191C]">{{ u.username }}</p>
              <p v-if="u.isMutual" class="text-xs text-[#FB7299] font-medium">互相关注</p>
            </div>
          </div>
          <FollowButton :user-id="u.id" size="small" />
        </div>
      </div>
      <el-empty v-else description="暂无数据" :image-size="60" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getFollowers, getFollowing } from '@/api/follow'
import FollowButton from '@/components/FollowButton.vue'
import type { FollowUser } from '@/types'

const route = useRoute()
const tab = ref('following')
const following = ref<FollowUser[]>([])
const followers = ref<FollowUser[]>([])

const users = computed(() => tab.value === 'following' ? following.value : followers.value)

onMounted(async () => {
  const userId = Number(route.params.userId ?? 0)
  try {
    const [fingRes, ferRes] = await Promise.all([
      getFollowing(userId),
      getFollowers(userId)
    ])
    following.value = fingRes.data?.list ?? (fingRes.data as any) ?? []
    followers.value = ferRes.data?.list ?? (ferRes.data as any) ?? []
  } catch { /* ignore */ }
})
</script>

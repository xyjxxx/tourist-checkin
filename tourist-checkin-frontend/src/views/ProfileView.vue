<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-4xl mx-auto p-6">
      <!-- User Info Card -->
      <div class="card-elevated overflow-hidden mb-6 animate-fade-in">
        <div
          class="h-36 relative group"
          :class="profileData?.backgroundImage ? '' : 'bg-gradient-to-r from-[#FB7299] via-[#FF85AB] to-[#FFA3BF]'"
          :style="profileData?.backgroundImage ? `background-image: url(${profileData.backgroundImage}); background-size: cover; background-position: center;` : ''"
        >
          <div class="absolute inset-0 opacity-10" style="background-image: radial-gradient(circle at 20% 50%, white 1px, transparent 1px); background-size: 24px 24px;" v-if="!profileData?.backgroundImage"></div>
          <!-- 自定义背景按钮（仅自己可见） -->
          <div v-if="isSelf" class="absolute inset-0 bg-black/0 group-hover:bg-black/30 transition-all flex items-center justify-center cursor-pointer" @click="triggerBgUpload">
            <div class="opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-2 px-4 py-2 bg-black/50 rounded-full backdrop-blur-sm">
              <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/>
                <circle cx="12" cy="13" r="3"/>
              </svg>
              <span class="text-white text-sm">{{ profileData?.backgroundImage ? '更换封面' : '设置封面' }}</span>
            </div>
            <div v-if="bgUploading" class="absolute inset-0 bg-black/50 flex items-center justify-center">
              <svg class="w-8 h-8 text-white animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10" stroke-opacity="0.3"/><path d="M12 2a10 10 0 019.95 9" stroke-linecap="round"/></svg>
            </div>
          </div>
          <input ref="bgInputRef" type="file" accept="image/*" class="hidden" @change="handleBgChange" />
        </div>
        <div class="px-6 pb-6 -mt-14 relative z-10">
          <div class="flex items-end gap-4">
            <div class="relative group cursor-pointer" v-if="isSelf" @click="triggerAvatarUpload">
              <el-avatar :size="88" :src="userStore.userInfo?.avatar || '/default-avatar.png'" class="ring-4 ring-white shadow-lg" />
              <div v-if="avatarUploading" class="absolute inset-0 rounded-full bg-black/50 flex items-center justify-center">
                <svg class="w-7 h-7 text-white animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" stroke-opacity="0.3"/>
                  <path d="M12 2a10 10 0 019.95 9" stroke-linecap="round"/>
                </svg>
              </div>
              <div v-else class="absolute inset-0 rounded-full bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center">
                <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <path d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/>
                  <circle cx="12" cy="13" r="3"/>
                </svg>
                <span class="text-white text-xs mt-1">更换头像</span>
              </div>
              <input ref="avatarInputRef" type="file" accept="image/*" class="hidden" @change="handleAvatarChange" />
            </div>
            <el-avatar v-else :size="88" :src="profileData?.avatar || '/default-avatar.png'" class="ring-4 ring-white shadow-lg" />
            <div class="flex-1 pb-1">
              <div class="flex items-center gap-3 flex-wrap">
                <div v-if="isSelf" class="group flex items-center gap-2 cursor-pointer" @click="openEditUsername">
                  <h1 class="text-2xl font-bold text-[#18191C] group-hover:text-[#FB7299] transition-colors">{{ profileData?.username }}</h1>
                  <div class="w-7 h-7 rounded-full bg-[#FB7299]/10 flex items-center justify-center opacity-60 group-hover:opacity-100 group-hover:bg-[#FB7299]/20 transition-all">
                    <svg class="w-3.5 h-3.5 text-[#FB7299]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                  </div>
                </div>
                <h1 v-else class="text-2xl font-bold text-[#18191C]">{{ profileData?.username }}</h1>
                <FollowButton v-if="!isSelf" :user-id="targetUserId" />
              </div>
              <p v-if="isSelf" class="text-[#9499A0] text-sm mt-1.5 flex items-center gap-1.5">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                账号: {{ profileData?.account || '' }}
              </p>
              <p v-if="isSelf && profileData?.email" class="text-[#9499A0] text-sm mt-1 flex items-center gap-1.5">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                {{ profileData.email }}
              </p>
              <p v-if="isSelf" class="text-xs text-[#9499A0] mt-1 flex items-center gap-1.5">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                加入于 {{ formatTime(profileData?.createdAt) }}
              </p>

              <!-- Follower/Following Stats -->
              <div class="flex items-center gap-6 mt-3">
                <div class="cursor-pointer hover:text-[#FB7299] transition-colors" @click="$router.push(`/follows/${targetUserId}`)">
                  <span class="font-bold text-[#18191C]">{{ followingCount }}</span>
                  <span class="text-sm text-[#9499A0] ml-1">关注</span>
                </div>
                <div class="cursor-pointer hover:text-[#FB7299] transition-colors" @click="$router.push(`/follows/${targetUserId}`)">
                  <span class="font-bold text-[#18191C]">{{ followerCount }}</span>
                  <span class="text-sm text-[#9499A0] ml-1">粉丝</span>
                </div>
                <div>
                  <span class="font-bold text-[#18191C]">{{ targetCheckIns.length }}</span>
                  <span class="text-sm text-[#9499A0] ml-1">打卡</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Points Display (self only) -->
      <div v-if="isSelf" class="card-elevated p-4 mb-6">
        <PointsDisplay />
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-3 gap-4 mb-6">
        <div class="card-elevated p-6 text-center group cursor-pointer hover:-translate-y-1 transition-transform duration-200">
          <div class="w-11 h-11 mx-auto mb-3 rounded-xl bg-[#FFF0F3] flex items-center justify-center">
            <svg class="w-5 h-5 text-[#FB7299]" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/></svg>
          </div>
          <p class="text-3xl font-extrabold text-[#FB7299]">{{ targetCheckIns.length }}</p>
          <p class="text-[#9499A0] text-sm mt-1">打卡次数</p>
        </div>
        <div class="card-elevated p-6 text-center group cursor-pointer hover:-translate-y-1 transition-transform duration-200">
          <div class="w-11 h-11 mx-auto mb-3 rounded-xl bg-green-50 flex items-center justify-center">
            <svg class="w-5 h-5 text-green-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M2 12h20"/></svg>
          </div>
          <p class="text-3xl font-extrabold text-green-600">{{ uniqueLocations }}</p>
          <p class="text-[#9499A0] text-sm mt-1">探索地点</p>
        </div>
        <div class="card-elevated p-6 text-center group cursor-pointer hover:-translate-y-1 transition-transform duration-200">
          <div class="w-11 h-11 mx-auto mb-3 rounded-xl bg-purple-50 flex items-center justify-center">
            <svg class="w-5 h-5 text-purple-500" viewBox="0 0 24 24" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
          </div>
          <p class="text-3xl font-extrabold text-purple-600">{{ totalLikes }}</p>
          <p class="text-[#9499A0] text-sm mt-1">收获点赞</p>
        </div>
      </div>

      <!-- Achievements (self only) -->
      <div v-if="isSelf" class="card-elevated p-6 mb-6">
        <AchievementBadge />
      </div>

      <!-- Quick Links (self only) -->
      <div v-if="isSelf" class="grid grid-cols-3 gap-3 mb-6">
        <el-button class="quick-link-btn" @click="$router.push('/achievements')">
          <el-icon class="mr-1.5"><Medal /></el-icon>
          成就徽章
        </el-button>
        <el-button class="quick-link-btn" @click="$router.push('/points')">
          <el-icon class="mr-1.5"><Coin /></el-icon>
          积分明细
        </el-button>
        <el-button class="quick-link-btn" @click="$router.push('/stats')">
          <el-icon class="mr-1.5"><DataAnalysis /></el-icon>
          数据统计
        </el-button>
      </div>

      <!-- Account Management (self only) -->
      <div v-if="isSelf" class="card-elevated p-6 mb-6">
        <h2 class="text-lg font-bold text-[#18191C] mb-4">账号管理</h2>
        <div class="flex gap-4">
          <el-button type="danger" plain @click="showDeleteAccountDialog" round>
            <el-icon class="mr-1"><Delete /></el-icon>
            注销账号
          </el-button>
          <el-button v-if="userStore.isAdmin" type="primary" @click="$router.push('/admin')" round>
            <el-icon class="mr-1"><Setting /></el-icon>
            管理后台
          </el-button>
        </div>
        <p class="text-sm text-[#9499A0] mt-3">
          注销账号后，您的所有数据将被删除，此操作不可恢复。
        </p>
      </div>

      <!-- Content Tabs -->
      <div class="card-elevated overflow-hidden">
        <el-tabs v-model="activeContentTab" @tab-change="handleTabChange" class="profile-tabs">
          <!-- 打卡记录 -->
          <el-tab-pane :label="isSelf ? '我的打卡' : 'TA的打卡'" name="checkins">
            <div v-loading="loading" class="p-6">
              <div v-if="!loading && targetCheckIns.length === 0" class="text-center py-12">
                <el-empty :description="isSelf ? '还没有打卡记录，快去添加吧~' : '该用户暂无打卡记录'" :image-size="80" />
              </div>
              <div v-else class="space-y-4">
                <div
                  v-for="item in targetCheckIns"
                  :key="item.id"
                  class="border-b border-[#F1F2F3] last:border-0 pb-4 last:pb-0"
                >
                  <div class="flex justify-between items-start">
                    <div class="flex-1 min-w-0">
                      <h3 class="font-semibold text-[#18191C]">{{ item.locationName }}</h3>
                      <el-tooltip :content="dayjs(item.checkInTime).format('YYYY-MM-DD HH:mm:ss')" placement="top">
                        <p class="text-sm text-[#9499A0] mt-0.5">{{ formatTime(item.checkInTime) }} · {{ dayjs(item.checkInTime).fromNow() }}</p>
                      </el-tooltip>
                      <p class="text-[#61666D] mt-2 text-sm leading-relaxed">{{ item.content || '无描述' }}</p>
                    </div>
                    <div class="flex items-center gap-2 ml-3">
                      <el-button
                        :type="item.hasLiked ? 'danger' : 'default'"
                        :icon="item.hasLiked ? StarFilled : Star"
                        size="small"
                        round
                        @click="handleLikeCheckIn(item)"
                      >
                        {{ item.likeCount || 0 }}
                      </el-button>
                      <el-button
                        v-if="isSelf"
                        type="danger"
                        link
                        size="small"
                        @click="handleDeleteCheckIn(item.id)"
                      >
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </div>
                  </div>
                  <div v-if="item.images?.length" class="flex gap-2 mt-3">
                    <el-image
                      v-for="(img, idx) in item.images"
                      :key="idx"
                      :src="img"
                      class="w-20 h-20 rounded-lg object-cover cursor-pointer"
                      :preview-src-list="item.images"
                      :initial-index="idx"
                    />
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 点赞的打卡 -->
          <el-tab-pane label="点赞的打卡" name="likedCheckins" v-if="isSelf">
            <div v-loading="likedCheckInsLoading" class="p-6">
              <div v-if="!likedCheckInsLoading && likedCheckIns.length === 0" class="text-center py-12">
                <el-empty description="还没有点赞的打卡记录" :image-size="80" />
              </div>
              <div v-else class="space-y-4">
                <div
                  v-for="item in likedCheckIns"
                  :key="item.id"
                  class="border-b border-[#F1F2F3] last:border-0 pb-4 last:pb-0"
                >
                  <div class="flex items-start gap-3">
                    <el-avatar :size="36" :src="item.avatar || '/default-avatar.png'" class="flex-shrink-0 cursor-pointer" @click="$router.push(`/profile/${item.userId}`)" />
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-2">
                        <span class="font-medium text-[#18191C] text-sm cursor-pointer hover:text-[#FB7299]" @click="$router.push(`/profile/${item.userId}`)">{{ item.username }}</span>
                        <span class="text-[#9499A0] text-xs">打卡了</span>
                        <span class="text-[#FB7299] font-medium text-sm">{{ item.locationName }}</span>
                      </div>
                      <p v-if="item.content" class="text-[#61666D] mt-1 text-sm line-clamp-2">{{ item.content }}</p>
                      <div v-if="item.images?.length" class="flex gap-1.5 mt-2">
                        <el-image v-for="(img, idx) in item.images.slice(0,3)" :key="idx" :src="img" class="w-14 h-14 rounded-lg object-cover" :preview-src-list="item.images" />
                      </div>
                      <p class="text-xs text-[#9499A0] mt-1.5 flex items-center gap-3">
                        <span>{{ formatTime(item.checkInTime) }}</span>
                        <span class="flex items-center gap-0.5"><el-icon :size="12"><Star /></el-icon>{{ item.likeCount }}</span>
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 点赞的游记 -->
          <el-tab-pane label="点赞的游记" name="likedNotes" v-if="isSelf">
            <div v-loading="likedNotesLoading" class="p-6">
              <div v-if="!likedNotesLoading && likedNotes.length === 0" class="text-center py-12">
                <el-empty description="还没有点赞的游记" :image-size="80" />
              </div>
              <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div
                  v-for="note in likedNotes"
                  :key="note.id"
                  class="border border-[#E3E5E7] rounded-xl overflow-hidden cursor-pointer hover:shadow-md transition-shadow"
                  @click="$router.push(`/travel-notes/${note.id}`)"
                >
                  <el-image v-if="note.coverImage" :src="note.coverImage" class="w-full h-36 object-cover" />
                  <div class="p-3">
                    <h3 class="font-semibold text-[#18191C] text-sm line-clamp-1">{{ note.title }}</h3>
                    <p v-if="note.summary" class="text-[#9499A0] text-xs mt-1 line-clamp-2">{{ note.summary }}</p>
                    <div class="flex items-center gap-3 mt-2 text-xs text-[#9499A0]">
                      <span class="flex items-center gap-0.5"><el-icon :size="12"><Star /></el-icon>{{ note.likeCount }}</span>
                      <span v-if="note.city">{{ note.city }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- Edit Username Dialog -->
    <el-dialog v-model="editUsernameVisible" title="修改昵称" width="420px" :close-on-click-modal="false">
      <div class="flex items-center gap-3 mb-5 p-3 bg-[#F1F2F3] rounded-xl">
        <el-avatar :size="40" :src="userStore.userInfo?.avatar || '/default-avatar.png'" />
        <div>
          <p class="text-xs text-[#9499A0]">当前昵称</p>
          <p class="font-semibold text-[#18191C]">{{ profileData?.username }}</p>
        </div>
      </div>
      <el-form @submit.prevent="confirmEditUsername">
        <el-form-item label="新昵称">
          <el-input
            v-model="editUsernameValue"
            placeholder="请输入新昵称"
            maxlength="20"
            show-word-limit
            size="large"
            clearable
          >
            <template #prefix>
              <svg class="w-4 h-4 text-[#9499A0]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <p class="text-xs text-[#9499A0] -mt-2">昵称将显示在你的个人主页和打卡记录中，最长20个字符</p>
      <template #footer>
        <el-button @click="editUsernameVisible = false" size="large">取消</el-button>
        <el-button type="primary" :loading="editUsernameLoading" @click="confirmEditUsername" size="large" round>
          确认修改
        </el-button>
      </template>
    </el-dialog>

    <!-- Delete Account Dialog -->
    <el-dialog v-model="deleteDialogVisible" title="注销账号" width="400px">
      <div class="text-[#61666D] mb-4">
        <p class="mb-2">注销后，您的所有数据（包括打卡记录、点赞等）将被永久删除。</p>
      </div>
      <el-form>
        <el-form-item label="请输入密码确认">
          <el-input
            v-model="deletePassword"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="deleteLoading" @click="confirmDeleteAccount">
          确认注销
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Star, StarFilled, Delete, Setting, Medal, Coin, DataAnalysis } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserCheckIns, deleteCheckIn, likeCheckIn, getLikedCheckIns } from '@/api/checkin'
import { getUserProfile, updateAvatar, updateUsername, updateBackground } from '@/api/user'
import { getLikedTravelNotes } from '@/api/travel-note'
import { uploadFiles } from '@/api/file'
import { compressImage } from '@/utils/image-compress'
import { getFollowers, getFollowing } from '@/api/follow'
import PointsDisplay from '@/components/PointsDisplay.vue'
import AchievementBadge from '@/components/AchievementBadge.vue'
import FollowButton from '@/components/FollowButton.vue'
import type { CheckIn, UserInfo } from '@/types'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const deleteDialogVisible = ref(false)
const deletePassword = ref('')
const deleteLoading = ref(false)
const loading = ref(false)
const avatarUploading = ref(false)
const avatarInputRef = ref<HTMLInputElement>()
const bgUploading = ref(false)
const bgInputRef = ref<HTMLInputElement>()
const editUsernameVisible = ref(false)
const editUsernameValue = ref('')
const editUsernameLoading = ref(false)

const profileData = ref<UserInfo | null>(null)
const targetCheckIns = ref<CheckIn[]>([])
const followerCount = ref(0)
const followingCount = ref(0)
const activeContentTab = ref('checkins')
const likedCheckIns = ref<CheckIn[]>([])
const likedNotes = ref<any[]>([])
const likedCheckInsLoading = ref(false)
const likedNotesLoading = ref(false)

const targetUserId = computed(() => {
  const id = route.params.userId
  return id ? Number(id) : userStore.userInfo?.id ?? 0
})

const isSelf = computed(() => targetUserId.value === userStore.userInfo?.id)

const formatTime = (time?: string) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const uniqueLocations = computed(() => {
  return new Set(targetCheckIns.value.map(c => `${c.longitude},${c.latitude}`)).size
})

const totalLikes = computed(() => {
  return targetCheckIns.value.reduce((sum, c) => sum + (c.likeCount || 0), 0)
})

const fetchProfile = async () => {
  const uid = targetUserId.value
  if (!uid) {
    loading.value = false
    return
  }

  loading.value = true
  targetCheckIns.value = []
  followerCount.value = 0
  followingCount.value = 0

  try {
    // 获取用户资料
    if (isSelf.value) {
      profileData.value = userStore.userInfo
    } else {
      try {
        const userRes = await getUserProfile(uid)
        profileData.value = userRes.data
      } catch (error: any) {
        if (error?.name !== 'CanceledError') {
          console.error('获取用户资料失败:', error)
        }
        profileData.value = { id: uid } as UserInfo
      }
    }

    // 获取打卡记录
    try {
      const res = await getUserCheckIns(uid)
      const checkIns = res.data
      if (Array.isArray(checkIns)) {
        targetCheckIns.value = checkIns
      } else {
        targetCheckIns.value = []
      }
    } catch (error: any) {
      if (error?.name !== 'CanceledError') {
        console.error('获取打卡记录失败:', error)
        ElMessage.error('获取打卡记录失败')
      }
      targetCheckIns.value = []
    }

    // 获取关注数据
    try {
      const [fingRes, ferRes] = await Promise.all([
        getFollowing(uid),
        getFollowers(uid)
      ])
      followingCount.value = Array.isArray(fingRes.data) ? fingRes.data.length : (fingRes.data?.total ?? 0)
      followerCount.value = Array.isArray(ferRes.data) ? ferRes.data.length : (ferRes.data?.total ?? 0)
    } catch { /* ignore */ }
  } finally {
    loading.value = false
  }
}

const showDeleteAccountDialog = () => {
  deletePassword.value = ''
  deleteDialogVisible.value = true
}

const confirmDeleteAccount = async () => {
  if (!deletePassword.value) {
    ElMessage.warning('请输入密码确认')
    return
  }

  deleteLoading.value = true
  try {
    await userStore.deleteAccountAction(deletePassword.value)
    ElMessage.success('账号已注销')
    deleteDialogVisible.value = false
    router.push('/login')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '注销失败')
  } finally {
    deleteLoading.value = false
  }
}

const openEditUsername = () => {
  editUsernameValue.value = profileData.value?.username || ''
  editUsernameVisible.value = true
}

const confirmEditUsername = async () => {
  if (!editUsernameValue.value.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  editUsernameLoading.value = true
  try {
    await updateUsername(editUsernameValue.value.trim())
    if (userStore.userInfo) {
      userStore.userInfo.username = editUsernameValue.value.trim()
    }
    if (profileData.value) {
      profileData.value.username = editUsernameValue.value.trim()
    }
    ElMessage.success('昵称修改成功')
    editUsernameVisible.value = false
  } catch (error: any) {
    ElMessage.error(error?.message || '修改失败')
  } finally {
    editUsernameLoading.value = false
  }
}

const triggerBgUpload = () => {
  bgInputRef.value?.click()
}

const handleBgChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB')
    return
  }

  bgUploading.value = true
  try {
    const compressed = await compressImage(file, {
      maxWidth: 1920,
      maxHeight: 500,
      quality: 0.85,
      maxSizeKB: 800
    })
    const uploadRes = await uploadFiles([compressed])
    const imageUrl = uploadRes.data.urls[0]
    await updateBackground(imageUrl)
    if (userStore.userInfo) {
      userStore.userInfo.backgroundImage = imageUrl
    }
    if (profileData.value) {
      profileData.value.backgroundImage = imageUrl
    }
    ElMessage.success('封面更新成功')
  } catch (error: any) {
    ElMessage.error(error?.message || '更新失败')
  } finally {
    bgUploading.value = false
    if (bgInputRef.value) bgInputRef.value.value = ''
  }
}

const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小 (最大5MB)
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  avatarUploading.value = true
  try {
    // 压缩图片
    const compressed = await compressImage(file, {
      maxWidth: 400,
      maxHeight: 400,
      quality: 0.85,
      maxSizeKB: 200
    })

    // 上传图片获取URL
    const uploadRes = await uploadFiles([compressed])
    const avatarUrl = uploadRes.data.urls[0]

    // 更新用户头像
    await updateAvatar(avatarUrl)

    // 更新本地状态
    if (userStore.userInfo) {
      userStore.userInfo.avatar = avatarUrl
    }
    if (profileData.value) {
      profileData.value.avatar = avatarUrl
    }

    ElMessage.success('头像更新成功')
  } catch (error: any) {
    ElMessage.error(error?.message || '头像更新失败')
  } finally {
    avatarUploading.value = false
    // 清除input值，允许重新选择同一文件
    if (avatarInputRef.value) {
      avatarInputRef.value.value = ''
    }
  }
}

const handleLikeCheckIn = async (item: CheckIn) => {
  try {
    await likeCheckIn(item.id)
    item.hasLiked = !item.hasLiked
    item.likeCount += item.hasLiked ? 1 : -1
  } catch (error: any) {
    ElMessage.error(error?.message || '操作失败')
  }
}

const handleTabChange = (tab: string) => {
  if (tab === 'likedCheckins' && likedCheckIns.value.length === 0) {
    loadLikedCheckIns()
  } else if (tab === 'likedNotes' && likedNotes.value.length === 0) {
    loadLikedNotes()
  }
}

const loadLikedCheckIns = async () => {
  likedCheckInsLoading.value = true
  try {
    const res = await getLikedCheckIns()
    likedCheckIns.value = Array.isArray(res.data) ? res.data : []
  } catch (error: any) {
    ElMessage.error(error?.message || '获取点赞打卡失败')
  } finally {
    likedCheckInsLoading.value = false
  }
}

const loadLikedNotes = async () => {
  likedNotesLoading.value = true
  try {
    const res = await getLikedTravelNotes()
    likedNotes.value = Array.isArray(res.data) ? res.data : []
  } catch (error: any) {
    ElMessage.error(error?.message || '获取点赞游记失败')
  } finally {
    likedNotesLoading.value = false
  }
}

const handleDeleteCheckIn = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条打卡记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCheckIn(id)
    targetCheckIns.value = targetCheckIns.value.filter(c => c.id !== id)
    ElMessage.success('删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

watch(() => route.params.userId, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    fetchProfile()
  }
})

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.quick-link-btn {
  height: 48px;
  border-radius: 12px;
  font-weight: 500;
  border: 1px solid #E3E5E7;
  transition: all 0.2s ease;
}

.quick-link-btn:hover {
  border-color: #FB7299;
  background: #FFF0F3;
}

.profile-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 24px;
  background: white;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: #E3E5E7;
}

.profile-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 20px;
  height: 50px;
  line-height: 50px;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: #FB7299;
  font-weight: 600;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background-color: #FB7299;
}
</style>

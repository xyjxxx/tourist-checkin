<template>
  <nav class="fixed top-0 left-0 right-0 z-50 h-14 bg-white border-b border-[#E3E5E7] shadow-[0_1px_3px_rgba(0,0,0,0.04)]">
    <div class="max-w-[1200px] h-full mx-auto px-6 flex items-center justify-between">
      <!-- Left: Logo + Nav Links -->
      <div class="flex items-center gap-1">
        <router-link to="/" class="flex items-center gap-2 mr-4">
          <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-[#FB7299] to-[#E85D88] flex items-center justify-center">
            <svg class="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
            </svg>
          </div>
          <span class="text-lg font-bold text-[#FB7299]">拾光旅记</span>
        </router-link>
        <router-link to="/" class="nav-link" active-class="nav-link--active" :exact="true">首页</router-link>
        <router-link to="/explore" class="nav-link" active-class="nav-link--active">发现</router-link>
        <router-link to="/travel-notes" class="nav-link" active-class="nav-link--active">游记</router-link>
        <router-link to="/trip-plans" class="nav-link" active-class="nav-link--active">行程</router-link>
      </div>

      <!-- Center: Search Bar -->
      <div class="flex-1 max-w-[320px] mx-8">
        <div class="relative">
          <input
            v-model="searchQuery"
            placeholder="搜索打卡、游记、用户..."
            class="w-full h-9 pl-4 pr-10 rounded-full bg-[#F1F2F3] text-sm border-none outline-none transition-all duration-200 focus:bg-white focus:ring-2 focus:ring-[#FB7299]/20 focus:shadow-[0_0_0_1px_rgba(251,114,153,0.3)]"
            @keyup.enter="handleSearch"
          />
          <button
            class="absolute right-1 top-1/2 -translate-y-1/2 w-7 h-7 rounded-full bg-[#FB7299] text-white flex items-center justify-center hover:bg-[#E85D88] transition-colors"
            @click="handleSearch"
          >
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Right: Actions -->
      <div class="flex items-center gap-3">
        <!-- Dark Mode Toggle -->
        <el-tooltip :content="isDark ? '切换亮色模式' : '切换深色模式'" placement="bottom">
          <button
            class="w-9 h-9 rounded-full bg-[#F1F2F3] dark:bg-gray-700 flex items-center justify-center hover:bg-[#E3E5E7] dark:hover:bg-gray-600 transition-colors"
            @click="toggleDark"
          >
            <svg v-if="isDark" class="w-4 h-4 text-yellow-400" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"/>
            </svg>
            <svg v-else class="w-4 h-4 text-[#61666D]" fill="currentColor" viewBox="0 0 24 24">
              <path d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"/>
            </svg>
          </button>
        </el-tooltip>

        <!-- Upload/Create Button -->
        <el-tooltip content="打卡" placement="bottom">
          <button
            class="w-9 h-9 rounded-full bg-[#FB7299]/10 text-[#FB7299] flex items-center justify-center hover:bg-[#FB7299]/20 transition-colors"
            @click="$router.push('/')"
          >
            <svg class="w-4.5 h-4.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path d="M12 5v14M5 12h14"/>
            </svg>
          </button>
        </el-tooltip>

        <!-- Notifications -->
        <NotificationBell />

        <!-- User Avatar Dropdown -->
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="flex items-center gap-2 cursor-pointer px-2 py-1 rounded-lg hover:bg-[#F1F2F3] transition-colors">
            <el-avatar :size="30" :src="userStore.userInfo?.avatar" class="flex-shrink-0">
              {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span class="text-sm font-medium text-[#18191C] max-w-[80px] truncate">{{ userStore.userInfo?.username }}</span>
            <svg class="w-3.5 h-3.5 text-[#9499A0]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path d="m6 9 6 6 6-6"/>
            </svg>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item command="achievements">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                我的成就
              </el-dropdown-item>
              <el-dropdown-item command="points">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
                积分中心
              </el-dropdown-item>
              <el-dropdown-item command="stats">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M18 20V10M12 20V4M6 20v-6"/></svg>
                数据统计
              </el-dropdown-item>
              <el-dropdown-item v-if="userStore.isAdmin" command="admin">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/></svg>
                管理后台
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/></svg>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useDarkMode } from '@/composables/useDarkMode'
import { ElMessage } from 'element-plus'
import NotificationBell from './NotificationBell.vue'

const router = useRouter()
const userStore = useUserStore()
const { isDark, toggle: toggleDark } = useDarkMode()
const searchQuery = ref('')

const handleSearch = () => {
  const q = searchQuery.value.trim()
  if (!q) {
    ElMessage.warning('请输入搜索内容')
    return
  }
  router.push({ path: '/explore', query: { q } })
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'achievements':
      router.push('/achievements')
      break
    case 'points':
      router.push('/points')
      break
    case 'stats':
      router.push('/stats')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
  }
}
</script>

<style scoped>
.nav-link {
  padding: 0 12px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  color: #61666D;
  border-radius: 8px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.nav-link:hover {
  color: #FB7299;
  background-color: #FFF0F3;
}

.nav-link--active {
  color: #FB7299;
  font-weight: 600;
}
</style>

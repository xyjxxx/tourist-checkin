<template>
  <div class="flex h-screen overflow-hidden bg-[#F5F5F7]">
    <!-- Sidebar -->
    <aside class="w-64 bg-white border-r border-[#E8E8ED] flex flex-col shrink-0">
      <div class="h-16 flex items-center px-6 border-b border-[#F0F0F2]">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center mr-3">
          <span class="text-white text-xs font-bold">拾</span>
        </div>
        <div>
          <span class="text-base font-bold text-[#1D1D1F]">拾光旅记</span>
          <span class="block text-[10px] text-[#AEAEB2] -mt-0.5">管理后台</span>
        </div>
      </div>
      <el-scrollbar class="flex-1">
        <el-menu :default-active="activeMenu" router :collapse="false"
                 active-text-color="#FB7299" background-color="#fff" text-color="#1D1D1F"
                 class="sidebar-menu">
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/checkins">
            <el-icon><Location /></el-icon>
            <span>打卡管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/travel-notes">
            <el-icon><Notebook /></el-icon>
            <span>游记管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/trip-plans">
            <el-icon><MapLocation /></el-icon>
            <span>行程管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/comments">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/topics">
            <el-icon><PriceTag /></el-icon>
            <span>话题管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/achievements">
            <el-icon><Trophy /></el-icon>
            <span>成就管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/merchants">
            <el-icon><Shop /></el-icon>
            <span>商户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/shop-recommends">
            <el-icon><Trophy /></el-icon>
            <span>店铺推荐审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/locations">
            <el-icon><Position /></el-icon>
            <span>地点管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/reports">
            <el-icon><Warning /></el-icon>
            <span>举报处理</span>
          </el-menu-item>
          <el-menu-item index="/admin/image-audits">
            <el-icon><Picture /></el-icon>
            <span>图片审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/sensitive-words">
            <el-icon><Mute /></el-icon>
            <span>敏感词管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/notifications">
            <el-icon><Bell /></el-icon>
            <span>系统通知</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
      <!-- User info -->
      <div class="h-14 flex items-center px-5 border-t border-[#F0F0F2]">
        <el-avatar :size="30" :src="userStore.userInfo?.avatar" />
        <span class="ml-2.5 text-sm truncate flex-1 font-medium">{{ userStore.userInfo?.username }}</span>
        <el-button text size="small" @click="handleLogout" class="!text-[#86868B]">
          <el-icon><SwitchButton /></el-icon>
        </el-button>
      </div>
    </aside>

    <!-- Main -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <header class="h-14 bg-white border-b border-[#E8E8ED] flex items-center px-6 shrink-0">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="ml-auto flex items-center gap-3">
          <el-tag type="danger" size="small" effect="dark" round>
            {{ userStore.userInfo?.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}
          </el-tag>
        </div>
      </header>
      <main class="flex-1 overflow-auto p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { disconnectWebSocket } from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const titleMap: Record<string, string> = {
  '/admin/dashboard': '数据概览',
  '/admin/users': '用户管理',
  '/admin/checkins': '打卡管理',
  '/admin/travel-notes': '游记管理',
  '/admin/trip-plans': '行程管理',
  '/admin/comments': '评论管理',
  '/admin/topics': '话题管理',
  '/admin/achievements': '成就管理',
  '/admin/merchants': '商户管理',
  '/admin/shop-recommends': '店铺推荐审核',
  '/admin/locations': '地点管理',
  '/admin/reports': '举报处理',
  '/admin/image-audits': '图片审核',
  '/admin/sensitive-words': '敏感词管理',
  '/admin/notifications': '系统通知',
}
const currentTitle = computed(() => titleMap[route.path] || '')

function handleLogout() {
  disconnectWebSocket()
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.sidebar-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.15s ease;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: #FFF0F3;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #FFF0F3;
  font-weight: 600;
}
</style>

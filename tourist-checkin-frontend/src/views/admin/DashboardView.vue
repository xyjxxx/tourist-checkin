<template>
  <div>
    <h2 class="text-2xl font-bold mb-6">小程序数据概览</h2>

    <!-- 加载失败重试 -->
    <div v-if="loadError" class="mb-4 p-4 bg-red-50 rounded-lg flex items-center justify-between">
      <span class="text-red-600">数据加载失败</span>
      <el-button size="small" type="primary" @click="loadDashboard">重新加载</el-button>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
      <div v-for="i in 6" :key="i" class="h-28 rounded-2xl bg-gray-100 animate-pulse" />
    </div>

    <template v-else>
    <!-- 核心指标卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
      <div v-for="card in statCards" :key="card.label"
           class="stat-card-gradient"
           :style="{ background: card.gradient }">
        <div class="w-12 h-12 rounded-xl bg-white/20 backdrop-blur-sm flex items-center justify-center">
          <el-icon :size="24" class="text-white"><component :is="card.icon" /></el-icon>
        </div>
        <div>
          <div class="text-3xl font-extrabold tracking-tight">{{ card.value }}</div>
          <div class="text-sm opacity-80 mt-1">{{ card.label }}</div>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <h3 class="text-lg font-bold mb-4">快捷操作</h3>
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
      <router-link v-for="shortcut in shortcuts" :key="shortcut.label" :to="shortcut.path"
                   class="bg-white rounded-2xl p-5 text-center shadow-card hover:shadow-card-hover
                          transition-all duration-200 no-underline hover:-translate-y-0.5 group">
        <div class="w-12 h-12 mx-auto mb-3 rounded-xl bg-primary-50 flex items-center justify-center
                    group-hover:bg-primary-100 transition-colors">
          <el-icon :size="22" class="text-primary-500"><component :is="shortcut.icon" /></el-icon>
        </div>
        <div class="text-sm font-medium text-[#1D1D1F]">{{ shortcut.label }}</div>
      </router-link>
    </div>

    <!-- 今日数据 & 系统状态 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-5">
      <div class="bg-white rounded-2xl p-6 shadow-card">
        <h4 class="font-bold mb-5 text-[#1D1D1F]">今日数据</h4>
        <div class="space-y-5">
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm text-[#86868B]">新增用户</span>
              <span class="text-lg font-bold text-primary-500">{{ overview.todayNewUsers || 0 }}</span>
            </div>
            <el-progress :percentage="todayUserPercent" :stroke-width="8" color="#FB7299" :show-text="false" />
          </div>
          <div>
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm text-[#86868B]">新增打卡</span>
              <span class="text-lg font-bold text-[#34C759]">{{ overview.todayCheckIns || 0 }}</span>
            </div>
            <el-progress :percentage="todayCheckinPercent" :stroke-width="8" color="#34C759" :show-text="false" />
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-6 shadow-card">
        <h4 class="font-bold mb-5 text-[#1D1D1F]">系统状态</h4>
        <div class="space-y-4">
          <div v-for="item in systemStatus" :key="item.label"
               class="flex justify-between items-center py-2">
            <div class="flex items-center gap-2">
              <span class="status-dot status-dot-success"></span>
              <span class="text-sm text-[#86868B]">{{ item.label }}</span>
            </div>
            <el-tag type="success" size="small" effect="light">{{ item.value }}</el-tag>
          </div>
        </div>
      </div>
    </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, Location, Document, MapLocation,
  ChatDotRound, Position, Bell, Trophy,
  Shop, Warning, Notebook
} from '@element-plus/icons-vue'
import { getAdminOverview } from '@/api/admin'
import type { AdminOverview } from '@/types'

const overview = ref<AdminOverview>({
  totalUsers: 0, totalCheckIns: 0, totalTravelNotes: 0,
  totalTripPlans: 0, totalComments: 0, totalLocations: 0,
  todayNewUsers: 0, todayCheckIns: 0
})

const statCards = computed(() => [
  { label: '小程序用户', value: overview.value.totalUsers, gradient: 'linear-gradient(135deg, #FB7299, #E85D88)', icon: User },
  { label: '打卡总数', value: overview.value.totalCheckIns, gradient: 'linear-gradient(135deg, #34C759, #248A3D)', icon: Location },
  { label: '游记总数', value: overview.value.totalTravelNotes, gradient: 'linear-gradient(135deg, #FF9500, #C77800)', icon: Document },
  { label: '行程总数', value: overview.value.totalTripPlans, gradient: 'linear-gradient(135deg, #FF3B30, #CC2D24)', icon: MapLocation },
  { label: '评论总数', value: overview.value.totalComments, gradient: 'linear-gradient(135deg, #5856D6, #4240B0)', icon: ChatDotRound },
  { label: '地点总数', value: overview.value.totalLocations, gradient: 'linear-gradient(135deg, #007AFF, #005EC4)', icon: Position },
])

const systemStatus = computed(() => [
  { label: '后端服务', value: loadError.value ? '异常' : '运行中' },
  { label: '小程序端', value: '已接入' },
  { label: '数据库', value: 'MySQL' },
  { label: '缓存', value: 'Redis' },
])

const todayUserPercent = computed(() => {
  if (!overview.value.totalUsers) return 0
  return Math.min(100, Math.round(overview.value.todayNewUsers / overview.value.totalUsers * 100))
})
const todayCheckinPercent = computed(() => {
  if (!overview.value.totalCheckIns) return 0
  return Math.min(100, Math.round(overview.value.todayCheckIns / overview.value.totalCheckIns * 100))
})

const shortcuts = [
  { label: '用户管理', path: '/admin/users', icon: User },
  { label: '打卡管理', path: '/admin/checkins', icon: Location },
  { label: '游记审核', path: '/admin/travel-notes', icon: Notebook },
  { label: '举报处理', path: '/admin/reports', icon: Warning },
  { label: '发送通知', path: '/admin/notifications', icon: Bell },
  { label: '话题管理', path: '/admin/topics', icon: Position },
  { label: '成就管理', path: '/admin/achievements', icon: Trophy },
  { label: '商户管理', path: '/admin/merchants', icon: Shop },
]

const loading = ref(true)
const loadError = ref(false)
const loadDashboard = async () => {
  loadError.value = false
  loading.value = true
  try {
    const res = await getAdminOverview()
    overview.value = res.data
  } catch {
    loadError.value = true
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}
onMounted(loadDashboard)
</script>

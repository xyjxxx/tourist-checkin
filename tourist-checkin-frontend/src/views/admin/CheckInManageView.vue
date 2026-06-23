<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">签到管理</h2>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-5 gap-3 mb-6" v-if="stats">
      <div class="bg-blue-50 rounded-lg p-3 text-center">
        <div class="text-xl font-bold text-blue-600">{{ stats.totalCheckIns }}</div>
        <div class="text-xs text-gray-500">签到总数</div>
      </div>
      <div class="bg-green-50 rounded-lg p-3 text-center">
        <div class="text-xl font-bold text-green-600">{{ stats.todayCheckIns }}</div>
        <div class="text-xs text-gray-500">今日签到</div>
      </div>
      <div class="bg-orange-50 rounded-lg p-3 text-center">
        <div class="text-xl font-bold text-orange-600">{{ stats.totalLikes }}</div>
        <div class="text-xs text-gray-500">总点赞数</div>
      </div>
      <div class="bg-purple-50 rounded-lg p-3 text-center">
        <div class="text-xl font-bold text-purple-600">{{ stats.totalUsers }}</div>
        <div class="text-xs text-gray-500">签到用户数</div>
      </div>
      <div class="bg-pink-50 rounded-lg p-3 text-center">
        <div class="text-xl font-bold text-pink-600">{{ stats.totalLocations }}</div>
        <div class="text-xs text-gray-500">签到地点数</div>
      </div>
    </div>

    <!-- 搜索 -->
    <div class="mb-4">
      <el-input v-model="keyword" placeholder="搜索内容、用户、地点" clearable style="width: 300px"
        @keyup.enter="handleSearch" @clear="handleSearch">
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="用户" width="150">
        <template #default="{ row }">
          <div class="flex items-center gap-2">
            <el-avatar :src="row.avatar" :size="28" />
            <span>{{ row.username }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="locationName" label="签到地点" min-width="160" />
      <el-table-column label="内容" min-width="200">
        <template #default="{ row }">
          <span class="line-clamp-2 text-sm text-gray-600">{{ row.content || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="likeCount" label="点赞数" width="90" />
      <el-table-column prop="checkInTime" label="签到时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <div class="py-12 text-center">
          <el-icon :size="48" class="text-gray-300 mb-3"><Location /></el-icon>
          <p class="text-gray-400 text-sm">暂无数据</p>
        </div>
      </template>
    </el-table>

    <!-- 分页 -->
    <div class="flex justify-end mt-4">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { getCheckInPage, adminDeleteCheckIn, getCheckInStats } from '@/api/checkin'
import type { CheckIn } from '@/types'

const tableData = ref<CheckIn[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const stats = ref<{ totalCheckIns: number; todayCheckIns: number; totalLikes: number; totalUsers: number; totalLocations: number } | null>(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCheckInPage(currentPage.value, pageSize.value, keyword.value || undefined)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await getCheckInStats()
    stats.value = res.data
  } catch {}
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleSizeChange = () => {
  currentPage.value = 1
  loadData()
}

const handleDelete = (row: CheckIn) => {
  ElMessageBox.confirm('确定删除该签到记录吗？', '删除确认', { type: 'warning' })
    .then(async () => {
      await adminDeleteCheckIn(row.id)
      ElMessage.success('删除成功')
      loadData()
      loadStats()
    })
    .catch((err: any) => {
      if (err !== 'cancel') ElMessage.error('删除失败')
    })
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

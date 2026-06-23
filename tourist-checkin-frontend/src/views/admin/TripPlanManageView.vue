<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">行程管理</h2>

    <!-- 搜索栏 -->
    <div class="mb-4 flex gap-3">
      <el-input v-model="keyword" placeholder="搜索行程标题" clearable style="width: 280px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="city" label="城市" width="120" />
      <el-table-column prop="totalDays" label="天数" width="80" />
      <el-table-column label="是否公开" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isPublic ? 'success' : 'info'" size="small">
            {{ row.isPublic ? '公开' : '私密' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <div class="py-12 text-center">
          <el-icon :size="48" class="text-gray-300 mb-3"><MapLocation /></el-icon>
          <p class="text-gray-400 text-sm">暂无数据</p>
        </div>
      </template>
    </el-table>

    <!-- 分页 -->
    <div class="mt-4 flex justify-end">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MapLocation } from '@element-plus/icons-vue'
import { getAdminTripPlans, adminDeleteTripPlan } from '@/api/admin'
import type { TripPlanBrief } from '@/types'

const keyword = ref('')
const tableData = ref<TripPlanBrief[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminTripPlans({ page: page.value, size: pageSize.value, keyword: keyword.value || undefined })
    tableData.value = res.data.list as TripPlanBrief[]
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleDelete = (row: import('@/types').TripPlanBrief) => {
  ElMessageBox.confirm(`确定删除行程「${row.title}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await adminDeleteTripPlan(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

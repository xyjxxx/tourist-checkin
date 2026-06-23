<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">评论管理</h2>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="用户ID" width="100">
        <template #default="{ row }">
          {{ row.user?.id || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="checkInId" label="签到ID" width="100" />
      <el-table-column label="评论内容" min-width="250">
        <template #default="{ row }">
          <span class="text-sm text-gray-600 line-clamp-2">{{ row.content }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="likeCount" label="点赞数" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '已禁用' }}
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
          <el-icon :size="48" class="text-gray-300 mb-3"><ChatDotRound /></el-icon>
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
import type { CommentItem } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { getAdminComments, adminDeleteComment } from '@/api/admin'

const tableData = ref<CommentItem[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminComments({ page: page.value, size: pageSize.value })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleDelete = (row: import('@/types').CommentItem) => {
  ElMessageBox.confirm('确定删除该评论吗？', '删除确认', { type: 'warning' })
    .then(async () => {
      await adminDeleteComment(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

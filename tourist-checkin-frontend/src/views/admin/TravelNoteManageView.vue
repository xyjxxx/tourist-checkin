<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">游记管理</h2>

    <!-- 筛选栏 -->
    <div class="mb-4 flex gap-3">
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 150px" @change="loadData">
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
        <el-option label="审核中" :value="-1" />
      </el-select>
      <el-input v-model="keyword" placeholder="搜索标题/关键词" clearable style="width: 240px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="作者" width="130">
        <template #default="{ row }">
          {{ row.author?.username || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="likeCount" label="点赞" width="80" />
      <el-table-column label="置顶" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isPinned" type="danger" size="small">置顶</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="发布时间" width="180" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === -1">
            <el-button size="small" type="success" @click="handleAudit(row, 1)">通过</el-button>
            <el-button size="small" type="warning" @click="handleAudit(row, -2)">驳回</el-button>
          </template>
          <el-button size="small" :type="row.isPinned ? 'info' : 'primary'" @click="handlePin(row)">
            {{ row.isPinned ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <div class="py-12 text-center">
          <el-icon :size="48" class="text-gray-300 mb-3"><Notebook /></el-icon>
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
import { Notebook } from '@element-plus/icons-vue'
import { getAdminTravelNotes, adminAuditTravelNote, adminDeleteTravelNote, adminTogglePinTravelNote } from '@/api/admin'
import type { TravelNote } from '@/types'

const keyword = ref('')
const statusFilter = ref<number | ''>('')
const tableData = ref<TravelNote[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusText = (s: number) => {
  if (s === 1) return '已发布'
  if (s === 0) return '草稿'
  if (s === -1) return '审核中'
  return '未知'
}
const statusTagType = (s: number) => {
  if (s === 1) return 'success'
  if (s === 0) return 'info'
  if (s === -1) return 'warning'
  return 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminTravelNotes({
      page: page.value,
      size: pageSize.value,
      status: statusFilter.value !== '' ? (statusFilter.value as number) : undefined,
      keyword: keyword.value || undefined,
    })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAudit = async (row: import('@/types').TravelNote, status: number) => {
  const action = status === 1 ? '通过' : '驳回'
  await ElMessageBox.confirm(`确定${action}该游记吗？`, '审核确认')
  await adminAuditTravelNote(row.id, status)
  ElMessage.success(`已${action}`)
  loadData()
}

const handlePin = async (row: import('@/types').TravelNote) => {
  await adminTogglePinTravelNote(row.id)
  ElMessage.success(row.isPinned ? '已取消置顶' : '已置顶')
  loadData()
}

const handleDelete = (row: import('@/types').TravelNote) => {
  ElMessageBox.confirm(`确定删除游记「${row.title}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await adminDeleteTravelNote(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">图片审核</h2>

    <!-- 筛选栏 -->
    <div class="mb-4 flex gap-3">
      <el-select v-model="statusFilter" placeholder="审核状态筛选" clearable style="width: 150px" @change="loadData">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="-1" />
      </el-select>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <el-image
            :src="row.imageUrl"
            style="width: 60px; height: 60px"
            fit="cover"
            :preview-src-list="[row.imageUrl]"
          />
        </template>
      </el-table-column>
      <el-table-column prop="sourceType" label="来源类型" width="110" />
      <el-table-column prop="sourceId" label="来源ID" width="100" />
      <el-table-column prop="uploaderId" label="上传者ID" width="110" />
      <el-table-column label="审核状态" width="110">
        <template #default="{ row }">
          <el-tag :type="auditStatusType(row.auditStatus)">{{ auditStatusText(row.auditStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="上传时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.auditStatus === 0">
            <el-button size="small" type="success" @click="handleAudit(row, 1)">通过</el-button>
            <el-button size="small" type="danger" @click="handleAudit(row, -1)">拒绝</el-button>
          </template>
          <span v-else class="text-gray-400 text-sm">已审核</span>
        </template>
      </el-table-column>
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
import { getAdminImageAudits, adminAuditImage } from '@/api/admin'

const statusFilter = ref<number | ''>('')
const tableData = ref<Record<string, unknown>[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const auditStatusText = (s: number) => {
  if (s === 0) return '待审核'
  if (s === 1) return '已通过'
  if (s === -1) return '已拒绝'
  return '未知'
}
const auditStatusType = (s: number) => {
  if (s === 0) return 'warning'
  if (s === 1) return 'success'
  if (s === -1) return 'danger'
  return 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminImageAudits({
      page: page.value,
      size: pageSize.value,
      status: statusFilter.value !== '' ? (statusFilter.value as number) : undefined,
    })
    tableData.value = res.data.list as Record<string, unknown>[]
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAudit = (row: Record<string, unknown>, auditStatus: number) => {
  const action = auditStatus === 1 ? '通过' : '拒绝'
  ElMessageBox.confirm(`确定${action}该图片吗？`, '审核确认')
    .then(async () => {
      await adminAuditImage(row.id as number, { auditStatus })
      ElMessage.success(`已${action}`)
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

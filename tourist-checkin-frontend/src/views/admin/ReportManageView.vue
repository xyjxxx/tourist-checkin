<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">举报管理</h2>

    <!-- 筛选栏 -->
    <div class="mb-4 flex gap-3">
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 150px" @change="loadData">
        <el-option label="待处理" :value="0" />
        <el-option label="已处理" :value="1" />
        <el-option label="已驳回" :value="-1" />
      </el-select>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="reporterId" label="举报人ID" width="110" />
      <el-table-column prop="reportedUserId" label="被举报人ID" width="120" />
      <el-table-column prop="targetType" label="举报类型" width="110" />
      <el-table-column prop="targetId" label="目标ID" width="100" />
      <el-table-column prop="reason" label="举报原因" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="reportStatusType(row.status)">{{ reportStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="举报时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="primary" @click="openHandle(row)">处理</el-button>
          <span v-else class="text-gray-400 text-sm">已处理</span>
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

    <!-- 处理对话框 -->
    <el-dialog v-model="handleVisible" title="处理举报" width="500px">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="处理结果">
          <el-select v-model="handleForm.status" style="width: 100%">
            <el-option label="确认违规" :value="1" />
            <el-option label="驳回举报" :value="-1" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="handleForm.handleResult" type="textarea" :rows="3" placeholder="请输入处理说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminReports, adminHandleReport } from '@/api/admin'

const statusFilter = ref<number | ''>('')
const tableData = ref<Record<string, unknown>[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const reportStatusText = (s: number) => {
  if (s === 0) return '待处理'
  if (s === 1) return '已处理'
  if (s === -1) return '已驳回'
  return '未知'
}
const reportStatusType = (s: number) => {
  if (s === 0) return 'warning'
  if (s === 1) return 'success'
  if (s === -1) return 'info'
  return 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminReports({
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

const handleVisible = ref(false)
const handleForm = reactive({ reportId: 0, status: 1, handleResult: '' })

const openHandle = (row: Record<string, unknown>) => {
  handleForm.reportId = row.id as number
  handleForm.status = 1
  handleForm.handleResult = ''
  handleVisible.value = true
}

const submitHandle = async () => {
  if (!handleForm.handleResult) {
    ElMessage.warning('请输入处理说明')
    return
  }
  await adminHandleReport(handleForm.reportId, { status: handleForm.status, handleResult: handleForm.handleResult })
  ElMessage.success('处理完成')
  handleVisible.value = false
  loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">通知管理</h2>

    <!-- 发送通知 -->
    <div class="mb-6 p-4 bg-gray-50 rounded-lg">
      <h3 class="text-lg font-semibold mb-3">发送通知</h3>
      <div class="flex flex-col gap-3">
        <div class="flex gap-3">
          <el-input v-model="sendForm.targetUserId" placeholder="目标用户ID" style="width: 200px" type="number" />
          <el-input v-model="sendForm.content" placeholder="通知内容" style="flex: 1" />
        </div>
        <div class="flex gap-3">
          <el-button type="primary" @click="handleSend">发送给指定用户</el-button>
          <el-button type="warning" @click="handleBroadcast">全员广播</el-button>
        </div>
      </div>
    </div>

    <!-- 通知列表 -->
    <h3 class="text-lg font-semibold mb-3">通知记录</h3>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="type" label="类型" width="110">
        <template #default="{ row }">
          <el-tag size="small">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
      <el-table-column label="已读" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isRead ? 'success' : 'info'" size="small">
            {{ row.isRead ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="发送时间" width="180" />
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
import { ref, reactive, onMounted } from 'vue'
import type { NotificationItem } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminNotifications, adminSendNotification, adminBroadcastNotification } from '@/api/admin'

const tableData = ref<NotificationItem[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const sendForm = reactive({ targetUserId: null as number | null, content: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminNotifications({ page: page.value, size: pageSize.value })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSend = async () => {
  if (!sendForm.targetUserId || !sendForm.content) {
    ElMessage.warning('请填写目标用户ID和通知内容')
    return
  }
  try {
    await ElMessageBox.confirm(`确定发送通知给用户 #${sendForm.targetUserId} 吗？`, '发送确认', { type: 'info' })
  } catch { return }
  await adminSendNotification(sendForm.targetUserId!, sendForm.content)
  ElMessage.success('发送成功')
  sendForm.targetUserId = null
  sendForm.content = ''
  loadData()
}

const handleBroadcast = () => {
  if (!sendForm.content) {
    ElMessage.warning('请填写通知内容')
    return
  }
  ElMessageBox.confirm('确定发送全员广播通知吗？', '广播确认', { type: 'warning' })
    .then(async () => {
      await adminBroadcastNotification(sendForm.content)
      ElMessage.success('广播发送成功')
      sendForm.targetUserId = null
      sendForm.content = ''
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

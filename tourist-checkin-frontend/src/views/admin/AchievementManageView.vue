<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">成就管理</h2>

    <!-- 操作栏 -->
    <div class="mb-4">
      <el-button type="success" @click="openCreate">新建成就</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="code" label="编码" width="140" />
      <el-table-column prop="name" label="名称" width="140" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="level" label="等级" width="80" />
      <el-table-column prop="conditionType" label="条件类型" width="120" />
      <el-table-column prop="conditionValue" label="条件值" width="100" />
      <el-table-column prop="pointsReward" label="积分奖励" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <div class="py-12 text-center">
          <el-icon :size="48" class="text-gray-300 mb-3"><Trophy /></el-icon>
          <p class="text-gray-400 text-sm">暂无数据</p>
        </div>
      </template>
    </el-table>

    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑成就' : '新建成就'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="编码">
          <el-input v-model="form.code" placeholder="如 CHECKIN_10" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入成就名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入成就描述" />
        </el-form-item>
        <el-form-item label="图标URL">
          <el-input v-model="form.icon" placeholder="请输入图标地址" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="如签到、游记、社交" />
        </el-form-item>
        <el-form-item label="等级">
          <el-input-number v-model="form.level" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="条件类型">
          <el-input v-model="form.conditionType" placeholder="如 TOTAL_CHECKINS" />
        </el-form-item>
        <el-form-item label="条件值">
          <el-input-number v-model="form.conditionValue" :min="0" />
        </el-form-item>
        <el-form-item label="积分奖励">
          <el-input-number v-model="form.pointsReward" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trophy } from '@element-plus/icons-vue'
import { getAdminAchievements, createAdminAchievement, updateAdminAchievement, deleteAdminAchievement } from '@/api/admin'
import type { AchievementItem } from '@/types'

const tableData = ref<AchievementItem[]>([])
const loading = ref(false)
const submitting = ref(false)

const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(0)
const form = reactive({
  code: '',
  name: '',
  description: '',
  icon: '',
  category: '',
  level: 1,
  conditionType: '',
  conditionValue: 0,
  pointsReward: 0,
})

const resetForm = () => {
  form.code = ''
  form.name = ''
  form.description = ''
  form.icon = ''
  form.category = ''
  form.level = 1
  form.conditionType = ''
  form.conditionValue = 0
  form.pointsReward = 0
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminAchievements()
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  isEditing.value = false
  dialogVisible.value = true
}

const openEdit = (row: import('@/types').AchievementItem) => {
  isEditing.value = true
  editingId.value = row.id
  form.code = row.code
  form.name = row.name
  form.description = row.description || ''
  form.icon = row.icon || ''
  form.category = row.category
  form.level = row.level
  form.conditionType = row.conditionType || ''
  form.conditionValue = row.conditionValue || 0
  form.pointsReward = row.pointsReward || 0
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.code || !form.name) {
    ElMessage.warning('请填写编码和名称')
    return
  }
  submitting.value = true
  try {
    const data = { ...form }
    if (isEditing.value) {
      await updateAdminAchievement(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createAdminAchievement(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row: import('@/types').AchievementItem) => {
  ElMessageBox.confirm(`确定删除成就「${row.name}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteAdminAchievement(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch((err: any) => {
      if (err !== 'cancel') ElMessage.error('删除失败')
    })
}

onMounted(loadData)
</script>

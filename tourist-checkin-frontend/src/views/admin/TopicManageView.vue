<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">话题管理</h2>

    <!-- 搜索栏 -->
    <div class="mb-4 flex gap-3">
      <el-input v-model="keyword" placeholder="搜索话题名称" clearable style="width: 240px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="openCreate">新建话题</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" width="140" />
      <el-table-column label="图标" width="80">
        <template #default="{ row }">
          <el-image v-if="row.icon" :src="row.icon" style="width: 32px; height: 32px" fit="cover" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="checkInCount" label="签到数" width="90" />
      <el-table-column prop="viewCount" label="浏览数" width="90" />
      <el-table-column label="热门" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isHot" type="danger" size="small">热门</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑话题' : '新建话题'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入话题名称" />
        </el-form-item>
        <el-form-item label="图标URL">
          <el-input v-model="form.icon" placeholder="请输入图标地址" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入话题描述" />
        </el-form-item>
        <el-form-item v-if="isEditing" label="热门">
          <el-switch v-model="form.isHot" :active-value="1" :inactive-value="0" />
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
import { getAdminTopics, createAdminTopic, updateAdminTopic, deleteAdminTopic } from '@/api/admin'
import type { TopicItem } from '@/types'

const keyword = ref('')
const tableData = ref<TopicItem[]>([])
const loading = ref(false)
const submitting = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(0)
const form = reactive({ name: '', icon: '', description: '', isHot: 0 })

const resetForm = () => {
  form.name = ''
  form.icon = ''
  form.description = ''
  form.isHot = 0
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminTopics({ page: page.value, size: pageSize.value, keyword: keyword.value || undefined })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  isEditing.value = false
  dialogVisible.value = true
}

const openEdit = (row: import('@/types').TopicItem) => {
  isEditing.value = true
  editingId.value = row.id
  form.name = row.name
  form.icon = row.icon || ''
  form.description = row.description || ''
  form.isHot = row.isHot ? 1 : 0
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.name) {
    ElMessage.warning('请输入话题名称')
    return
  }
  submitting.value = true
  try {
    if (isEditing.value) {
      await updateAdminTopic(editingId.value, { name: form.name, icon: form.icon, description: form.description, isHot: form.isHot })
      ElMessage.success('更新成功')
    } else {
      await createAdminTopic({ name: form.name, icon: form.icon, description: form.description })
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

const handleDelete = (row: import('@/types').TopicItem) => {
  ElMessageBox.confirm(`确定删除话题「${row.name}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteAdminTopic(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch((err: any) => {
      if (err !== 'cancel') ElMessage.error('删除失败')
    })
}

onMounted(loadData)
</script>

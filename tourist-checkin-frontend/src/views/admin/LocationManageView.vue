<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">地点管理</h2>

    <!-- 操作栏 -->
    <div class="mb-4 flex gap-3">
      <el-button type="success" @click="openCreate">新建地点</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="city" label="城市" width="120" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="longitude" label="经度" width="120" />
      <el-table-column prop="latitude" label="纬度" width="120" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <div class="py-12 text-center">
          <el-icon :size="48" class="text-gray-300 mb-3"><LocationIcon /></el-icon>
          <p class="text-gray-400 text-sm">暂无数据</p>
        </div>
      </template>
    </el-table>

    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑地点' : '新建地点'" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入地点名称" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="如景点、餐厅、酒店" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number v-model="form.longitude" :precision="6" :step="0.001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="form.latitude" :precision="6" :step="0.001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入地点描述" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="请输入封面图URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location as LocationIcon } from '@element-plus/icons-vue'
import { getAllLocations } from '@/api/location'
import { createAdminLocation, updateAdminLocation, deleteAdminLocation } from '@/api/admin'
import type { Location } from '@/types'

const tableData = ref<Location[]>([])
const loading = ref(false)
const submitting = ref(false)

const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(0)
const form = reactive({
  name: '',
  address: '',
  city: '',
  category: '',
  longitude: 0,
  latitude: 0,
  description: '',
  coverImage: '',
})

const resetForm = () => {
  Object.assign(form, { name: '', address: '', city: '', category: '', longitude: 0, latitude: 0, description: '', coverImage: '' })
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAllLocations()
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

const openEdit = (row: Location) => {
  isEditing.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    address: row.address,
    city: row.city,
    category: row.category,
    longitude: row.longitude,
    latitude: row.latitude,
    description: row.description || '',
    coverImage: row.coverImage || '',
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.name) {
    ElMessage.warning('请输入地点名称')
    return
  }
  submitting.value = true
  try {
    const data = { ...form }
    if (isEditing.value) {
      await updateAdminLocation(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createAdminLocation(data)
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

const handleDelete = (row: Location) => {
  ElMessageBox.confirm(`确定删除地点「${row.name}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteAdminLocation(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => { ElMessage.error('删除失败') })
}

onMounted(loadData)
</script>

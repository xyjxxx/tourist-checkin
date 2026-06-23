<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">商户管理</h2>

    <!-- 搜索栏 -->
    <div class="mb-4 flex gap-3">
      <el-input v-model="keyword" placeholder="搜索商户名称" clearable style="width: 240px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="categoryFilter" placeholder="分类筛选" clearable style="width: 150px" @change="loadData">
        <el-option label="餐饮" value="餐饮" />
        <el-option label="住宿" value="住宿" />
        <el-option label="景点" value="景点" />
        <el-option label="购物" value="购物" />
        <el-option label="娱乐" value="娱乐" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="openCreate">新建商户</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="rating" label="评分" width="80" />
      <el-table-column prop="priceLevel" label="价位" width="80">
        <template #default="{ row }">
          {{ '¥'.repeat(row.priceLevel || 0) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '营业中' : '已关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
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
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑商户' : '新建商户'" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入商户名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option label="餐饮" value="餐饮" />
            <el-option label="住宿" value="住宿" />
            <el-option label="景点" value="景点" />
            <el-option label="购物" value="购物" />
            <el-option label="娱乐" value="娱乐" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number v-model="form.longitude" :precision="6" :step="0.001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="form.latitude" :precision="6" :step="0.001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="评分">
          <el-input-number v-model="form.rating" :min="0" :max="5" :precision="1" :step="0.1" />
        </el-form-item>
        <el-form-item label="价位">
          <el-input-number v-model="form.priceLevel" :min="1" :max="5" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="form.businessHours" placeholder="如 09:00-22:00" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="请输入封面图URL" />
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
import type { MerchantPosition } from '@/types'
import { getAdminMerchants, createAdminMerchant, updateAdminMerchant, deleteAdminMerchant } from '@/api/admin'

const keyword = ref('')
const categoryFilter = ref('')
const tableData = ref<MerchantPosition[]>([])
const loading = ref(false)
const submitting = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(0)
const form = reactive({
  name: '',
  category: '',
  address: '',
  longitude: 0,
  latitude: 0,
  rating: 4.0,
  priceLevel: 1,
  phone: '',
  businessHours: '',
  description: '',
  coverImage: '',
})

const resetForm = () => {
  Object.assign(form, { name: '', category: '', address: '', longitude: 0, latitude: 0, rating: 4.0, priceLevel: 1, phone: '', businessHours: '', description: '', coverImage: '' })
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminMerchants({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      category: categoryFilter.value || undefined,
    })
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

const openEdit = (row: import('@/types').MerchantPosition) => {
  isEditing.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    category: row.category,
    address: row.address || '',
    longitude: row.longitude,
    latitude: row.latitude,
    rating: row.rating,
    priceLevel: row.priceLevel,
    phone: row.phone || '',
    businessHours: row.businessHours || '',
    description: row.description || '',
    coverImage: row.coverImage || '',
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.name || !form.category) {
    ElMessage.warning('请填写商户名称和分类')
    return
  }
  submitting.value = true
  try {
    const data = { ...form }
    if (isEditing.value) {
      await updateAdminMerchant(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createAdminMerchant(data)
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

const handleDelete = (row: import('@/types').MerchantPosition) => {
  ElMessageBox.confirm(`确定删除商户「${row.name}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteAdminMerchant(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch((err: any) => {
      if (err !== 'cancel') ElMessage.error('删除失败')
    })
}

onMounted(loadData)
</script>

<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">敏感词管理</h2>

    <!-- 添加表单 -->
    <div class="mb-4 flex gap-3 items-end">
      <el-input v-model="addForm.word" placeholder="敏感词" clearable style="width: 200px" />
      <el-select v-model="addForm.category" placeholder="分类" clearable style="width: 140px">
        <el-option label="政治" value="政治" />
        <el-option label="色情" value="色情" />
        <el-option label="暴力" value="暴力" />
        <el-option label="广告" value="广告" />
        <el-option label="其他" value="其他" />
      </el-select>
      <el-select v-model="addForm.level" placeholder="等级" style="width: 120px">
        <el-option label="低" :value="1" />
        <el-option label="中" :value="2" />
        <el-option label="高" :value="3" />
      </el-select>
      <el-button type="success" @click="handleAdd">添加</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="word" label="敏感词" min-width="200" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column label="等级" width="100">
        <template #default="{ row }">
          <el-tag :type="levelType(row.level)">{{ levelText(row.level) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminSensitiveWords, addAdminSensitiveWord, deleteAdminSensitiveWord } from '@/api/admin'

const tableData = ref<Record<string, unknown>[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const addForm = reactive({ word: '', category: '', level: 1 })

const levelText = (l: number) => {
  if (l === 1) return '低'
  if (l === 2) return '中'
  if (l === 3) return '高'
  return '未知'
}
const levelType = (l: number) => {
  if (l === 1) return 'info'
  if (l === 2) return 'warning'
  if (l === 3) return 'danger'
  return 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminSensitiveWords({ page: page.value, size: pageSize.value })
    tableData.value = res.data.list as Record<string, unknown>[]
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = async () => {
  if (!addForm.word.trim()) {
    ElMessage.warning('请输入敏感词')
    return
  }
  await addAdminSensitiveWord({
    word: addForm.word.trim(),
    category: addForm.category || undefined,
    level: addForm.level,
  })
  ElMessage.success('添加成功')
  addForm.word = ''
  addForm.category = ''
  addForm.level = 1
  loadData()
}

const handleDelete = (row: Record<string, unknown>) => {
  ElMessageBox.confirm(`确定删除敏感词「${row.word}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteAdminSensitiveWord(row.id as number)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

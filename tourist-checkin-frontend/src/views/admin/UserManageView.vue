<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-2xl font-bold">小程序账号管理</h2>
      <el-tag type="success" effect="dark">共 {{ total }} 个账号</el-tag>
    </div>

    <!-- 搜索与筛选 -->
    <div class="mb-4 flex gap-3 flex-wrap">
      <el-input v-model="keyword" placeholder="搜索用户名/账号/邮箱" clearable style="width: 260px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="loginType" placeholder="登录方式" clearable style="width: 140px" @change="loadData">
        <el-option label="全部" value="" />
        <el-option label="微信用户" value="wechat" />
        <el-option label="账号密码" value="account" />
      </el-select>
      <el-select v-model="roleFilter" placeholder="角色" clearable style="width: 140px" @change="loadData">
        <el-option label="全部" value="" />
        <el-option label="普通用户" value="USER" />
        <el-option label="管理员" value="ADMIN" />
        <el-option label="超级管理员" value="SUPER_ADMIN" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button @click="handleExport">导出 CSV</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading" row-key="id">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="用户" min-width="160">
        <template #default="{ row }">
          <div class="flex items-center gap-2">
            <el-avatar :src="row.avatar" :size="36">
              {{ row.username?.charAt(0) }}
            </el-avatar>
            <div>
              <div class="font-medium">{{ row.username }}</div>
              <div class="text-xs text-gray-400">{{ row.account || '无账号' }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="登录方式" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.openid" type="success" size="small" effect="plain">
            <span class="mr-1">📱</span>微信
          </el-tag>
          <el-tag v-else type="info" size="small" effect="plain">
            <span class="mr-1">🔑</span>账号
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="openid" label="OpenID" min-width="180">
        <template #default="{ row }">
          <span v-if="row.openid" class="text-xs text-gray-500 font-mono">
            {{ row.openid.substring(0, 16) }}...
          </span>
          <span v-else class="text-xs text-gray-300">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="160">
        <template #default="{ row }">
          <span>{{ row.email || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.role)" size="small">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170">
        <template #default="{ row }">
          <span class="text-sm">{{ formatDateTime(row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">详情</el-button>
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="openResetPwd(row)">重置密码</el-button>
          <el-popconfirm title="确定删除该账号？" @confirm="handleDelete(row)" confirm-button-text="删除" cancel-button-text="取消">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="mt-4 flex justify-end">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
      />
    </div>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="520px">
      <div v-if="detailUser" class="space-y-4">
        <div class="flex items-center gap-4">
          <el-avatar :src="detailUser.avatar" :size="64">{{ detailUser.username?.charAt(0) }}</el-avatar>
          <div>
            <div class="text-lg font-bold">{{ detailUser.username }}</div>
            <el-tag :type="roleTagType(detailUser.role)" size="small">{{ roleLabel(detailUser.role) }}</el-tag>
          </div>
        </div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="用户 ID">{{ detailUser.id }}</el-descriptions-item>
          <el-descriptions-item label="登录账号">{{ detailUser.account || '微信登录（无账号）' }}</el-descriptions-item>
          <el-descriptions-item label="登录方式">
            <el-tag v-if="detailUser.openid" type="success" size="small">微信登录</el-tag>
            <el-tag v-else type="info" size="small">账号密码</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="OpenID">{{ detailUser.openid || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detailUser.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDateTime(detailUser.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editVisible" title="编辑用户" width="450px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="用户邮箱" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPwdVisible" title="重置密码" width="400px">
      <el-alert v-if="resetPwdTarget?.openid && !resetPwdTarget?.account" type="info" :closable="false" class="mb-4">
        该用户为微信登录用户，重置密码后可使用账号密码登录。
      </el-alert>
      <el-form :model="resetPwdForm" label-width="80px">
        <el-form-item label="新密码">
          <el-input v-model="resetPwdForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetPwdSubmitting" @click="submitResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, adminDeleteUser, adminUpdateUser, adminResetPassword } from '@/api/user'
import type { AdminUser } from '@/types'

const keyword = ref('')
const loginType = ref('')
const roleFilter = ref('')
const tableData = ref<AdminUser[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const roleTagType = (role: string) => {
  if (role === 'SUPER_ADMIN') return 'danger'
  if (role === 'ADMIN') return 'warning'
  return 'info'
}
const roleLabel = (role: string) => {
  if (role === 'SUPER_ADMIN') return '超级管理员'
  if (role === 'ADMIN') return '管理员'
  return '普通用户'
}
const formatDateTime = (dt: string) => {
  if (!dt) return '-'
  return dt.replace('T', ' ').substring(0, 19)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList(page.value, pageSize.value, keyword.value || undefined, loginType.value || undefined, roleFilter.value || undefined)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

// --- 详情 ---
const detailVisible = ref(false)
const detailUser = ref<AdminUser | null>(null)
const openDetail = (row: AdminUser) => {
  detailUser.value = row
  detailVisible.value = true
}

// --- 编辑 ---
const editVisible = ref(false)
const editForm = reactive({ userId: 0, email: '', role: '' })
const openEdit = (row: AdminUser) => {
  editForm.userId = row.id
  editForm.email = row.email || ''
  editForm.role = row.role
  editVisible.value = true
}
const editSubmitting = ref(false)
const submitEdit = async () => {
  if (editForm.role === 'SUPER_ADMIN') {
    try {
      await ElMessageBox.confirm('确定将该用户提升为超级管理员？此操作权限极高，请谨慎操作。', '权限变更确认', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
    } catch {
      return
    }
  }
  editSubmitting.value = true
  try {
    await adminUpdateUser(editForm.userId, { email: editForm.email, role: editForm.role })
    ElMessage.success('更新成功')
    editVisible.value = false
    loadData()
  } catch {
    // 错误已在 request 拦截器中处理
  } finally {
    editSubmitting.value = false
  }
}

// --- 重置密码 ---
const resetPwdVisible = ref(false)
const resetPwdForm = reactive({ userId: 0, newPassword: '' })
const resetPwdTarget = ref<AdminUser | null>(null)
const openResetPwd = (row: AdminUser) => {
  resetPwdTarget.value = row
  resetPwdForm.userId = row.id
  resetPwdForm.newPassword = ''
  resetPwdVisible.value = true
}
const resetPwdSubmitting = ref(false)
const submitResetPwd = async () => {
  if (!resetPwdForm.newPassword || resetPwdForm.newPassword.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  resetPwdSubmitting.value = true
  try {
    await adminResetPassword(resetPwdForm.userId, resetPwdForm.newPassword)
    ElMessage.success('密码重置成功')
    resetPwdVisible.value = false
  } catch {
    // 错误已在 request 拦截器中处理
  } finally {
    resetPwdSubmitting.value = false
  }
}

// --- 删除 ---
const handleDelete = async (row: AdminUser) => {
  await adminDeleteUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

// --- 导出 ---
const escapeCsvCell = (val: string) => {
  if (!val) return val
  if (/^[=+\-@]/.test(val)) return "'" + val
  return val
}
const handleExport = () => {
  const header = 'ID,用户名,账号,登录方式,OpenID,邮箱,角色,注册时间\n'
  const rows = tableData.value.map(r =>
    [r.id, escapeCsvCell(r.username), escapeCsvCell(r.account || ''), r.openid ? '微信' : '账号', r.openid || '', escapeCsvCell(r.email || ''), roleLabel(r.role), r.createdAt].join(',')
  ).join('\n')
  const bom = '﻿'
  const blob = new Blob([bom + header + rows], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '小程序账号列表.csv'
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(loadData)
</script>

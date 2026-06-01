<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-7xl mx-auto p-6">
      <!-- Page Title -->
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-3">
          <h1 class="text-xl font-bold text-[#18191C]">管理后台</h1>
          <el-tag v-if="userStore.isSuperAdmin" type="warning" round>超级管理员</el-tag>
          <el-tag v-else-if="userStore.isAdmin" type="danger" round>管理员</el-tag>
        </div>
        <el-dropdown @command="handleExport">
          <el-button type="primary" round>
            <el-icon class="mr-1"><Download /></el-icon>
            导出数据
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="users">导出用户列表</el-dropdown-item>
              <el-dropdown-item command="checkins">导出打卡记录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-5 gap-4 mb-6">
        <div class="card-elevated p-5 text-center relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#FF85AB] to-[#FB7299]"></div>
          <p class="text-3xl font-extrabold text-[#FB7299] mt-2">{{ stats.userCount }}</p>
          <p class="text-[#9499A0] text-sm mt-1">总用户数</p>
        </div>
        <div class="card-elevated p-5 text-center relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-green-400 to-green-600"></div>
          <p class="text-3xl font-extrabold text-green-600 mt-2">{{ stats.checkInCount }}</p>
          <p class="text-[#9499A0] text-sm mt-1">总打卡数</p>
        </div>
        <div class="card-elevated p-5 text-center relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-purple-400 to-purple-600"></div>
          <p class="text-3xl font-extrabold text-purple-600 mt-2">{{ stats.totalLikes }}</p>
          <p class="text-[#9499A0] text-sm mt-1">总点赞数</p>
        </div>
        <div class="card-elevated p-5 text-center relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-orange-400 to-orange-600"></div>
          <p class="text-3xl font-extrabold text-orange-600 mt-2">{{ stats.locationCount }}</p>
          <p class="text-[#9499A0] text-sm mt-1">地点数量</p>
        </div>
        <div class="card-elevated p-5 text-center relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-red-400 to-red-600"></div>
          <p class="text-3xl font-extrabold text-red-600 mt-2">{{ stats.todayCheckIn }}</p>
          <p class="text-[#9499A0] text-sm mt-1">今日打卡</p>
        </div>
      </div>

      <!-- Tabs -->
      <div class="card-elevated p-6">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="用户管理" name="users">
            <div class="flex justify-between items-center mb-4">
              <h2 class="text-xl font-bold text-[#18191C]">用户管理</h2>
              <div>
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索用户名/邮箱"
                  style="width: 260px"
                  clearable
                  @keyup.enter="handleSearch"
                >
                  <template #append>
                    <el-button :icon="Search" @click="handleSearch" />
                  </template>
                </el-input>
              </div>
            </div>

            <el-table v-loading="loading" :data="userList" border stripe class="admin-table">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column label="用户信息" min-width="200">
                <template #default="{ row }">
                  <div class="flex items-center gap-3">
                    <el-avatar :size="36" :src="row.avatar || '/default-avatar.png'" />
                    <div>
                      <p class="font-semibold">{{ row.username }}</p>
                      <p class="text-xs text-[#9499A0]">{{ row.email || '无邮箱' }}</p>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="角色" width="120">
                <template #default="{ row }">
                  <el-tag v-if="row.role === 'SUPER_ADMIN'" type="warning" size="small" round>超级管理员</el-tag>
                  <el-tag v-else-if="row.role === 'ADMIN'" type="danger" size="small" round>管理员</el-tag>
                  <el-tag v-else type="info" size="small" round>普通用户</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="注册时间" width="180">
                <template #default="{ row }">
                  {{ formatTime(row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="260" fixed="right">
                <template #default="{ row }">
                  <!-- 超级管理员不能被任何人操作，普通管理员只有超管能操作 -->
                  <template v-if="row.role === 'SUPER_ADMIN'">
                    <span class="text-xs text-[#9499A0]">不可操作</span>
                  </template>
                  <template v-else-if="row.role === 'ADMIN' && !userStore.isSuperAdmin">
                    <span class="text-xs text-[#9499A0]">需要超级管理员权限</span>
                  </template>
                  <template v-else>
                    <el-button link type="primary" size="small" @click="handleEditUser(row)">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-button link type="warning" size="small" @click="handleResetPassword(row)">
                      <el-icon><Key /></el-icon>
                      重置密码
                    </el-button>
                    <el-button
                      v-if="row.role !== 'ADMIN'"
                      link
                      type="danger"
                      size="small"
                      @click="handleDeleteUser(row)"
                    >
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>
                </template>
              </el-table-column>
            </el-table>

            <div class="flex justify-end mt-4">
              <el-pagination
                v-model:current-page="page"
                v-model:page-size="size"
                :total="total"
                :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next"
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
              />
            </div>
          </el-tab-pane>

          <el-tab-pane label="打卡管理" name="checkins">
            <div class="flex justify-between items-center mb-4">
              <h2 class="text-xl font-bold text-[#18191C]">打卡管理</h2>
              <el-button type="primary" @click="loadAllCheckIns" :loading="checkInLoading" round>
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>

            <el-table v-loading="checkInLoading" :data="checkInList" border stripe class="admin-table">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column label="用户信息" width="150">
                <template #default="{ row }">
                  <div class="flex items-center gap-2">
                    <el-avatar :size="28" :src="row.avatar || '/default-avatar.png'" />
                    <span class="text-sm">{{ row.username }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="打卡地点" min-width="150">
                <template #default="{ row }">
                  <p class="font-medium">{{ row.locationName }}</p>
                  <p class="text-xs text-[#9499A0]">{{ row.longitude }}, {{ row.latitude }}</p>
                </template>
              </el-table-column>
              <el-table-column label="内容" min-width="200">
                <template #default="{ row }">
                  <p class="line-clamp-2 text-sm">{{ row.content || '无内容' }}</p>
                </template>
              </el-table-column>
              <el-table-column label="点赞" width="90" align="center">
                <template #default="{ row }">
                  <span class="flex items-center gap-1 justify-center">
                    <el-icon class="text-orange-500"><Star /></el-icon>
                    {{ row.likeCount || 0 }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="打卡时间" width="180">
                <template #default="{ row }">
                  {{ formatTime(row.checkInTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button link type="danger" size="small" @click="handleDeleteCheckIn(row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- Edit User Dialog -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="420px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" placeholder="选择角色">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
            <el-option v-if="userStore.isSuperAdmin" label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false" round>取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="confirmEditUser" round>保存</el-button>
      </template>
    </el-dialog>

    <!-- Reset Password Dialog -->
    <el-dialog v-model="resetPwdDialogVisible" title="重置密码" width="420px">
      <el-form :model="resetPwdForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="resetPwdForm.username" disabled />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="resetPwdForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdDialogVisible = false" round>取消</el-button>
        <el-button type="primary" :loading="resetPwdLoading" @click="confirmResetPassword" round>
          确认重置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Edit, Delete, Key, Refresh, Star, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserList, adminDeleteUser, adminUpdateUser, adminResetPassword } from '@/api/user'
import { getAllCheckIns, adminDeleteCheckIn, getCheckInStats } from '@/api/checkin'
import { getAllLocations } from '@/api/location'
import type { AdminUser, CheckIn } from '@/types'
import { formatDateSafe } from '@/utils/date'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('users')

const loading = ref(false)
const userList = ref<AdminUser[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const searchKeyword = ref('')

const checkInLoading = ref(false)
const checkInList = ref<CheckIn[]>([])

const editDialogVisible = ref(false)
const editLoading = ref(false)
const editForm = ref({
  id: 0,
  username: '',
  email: '',
  role: 'USER'
})

const resetPwdDialogVisible = ref(false)
const resetPwdLoading = ref(false)
const resetPwdForm = ref({
  id: 0,
  username: '',
  newPassword: ''
})

const stats = ref({
  userCount: 0,
  checkInCount: 0,
  totalLikes: 0,
  locationCount: 0,
  todayCheckIn: 0
})

const formatTime = (time?: string) => {
  return formatDateSafe(time, 'YYYY-MM-DD HH:mm')
}

const loadUserList = async () => {
  loading.value = true
  try {
    const res = await getUserList(page.value, size.value, searchKeyword.value || undefined)
    userList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadUserList()
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadUserList()
}

const handleSizeChange = (newSize: number) => {
  size.value = newSize
  page.value = 1
  loadUserList()
}

const handleEditUser = (user: AdminUser) => {
  editForm.value = {
    id: user.id,
    username: user.username,
    email: user.email || '',
    role: user.role || 'USER'
  }
  editDialogVisible.value = true
}

const confirmEditUser = async () => {
  editLoading.value = true
  try {
    await adminUpdateUser(editForm.value.id, {
      email: editForm.value.email,
      role: editForm.value.role
    })
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    loadUserList()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '修改失败')
  } finally {
    editLoading.value = false
  }
}

const handleResetPassword = (user: AdminUser) => {
  resetPwdForm.value = {
    id: user.id,
    username: user.username,
    newPassword: ''
  }
  resetPwdDialogVisible.value = true
}

const confirmResetPassword = async () => {
  if (!resetPwdForm.value.newPassword || resetPwdForm.value.newPassword.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }
  resetPwdLoading.value = true
  try {
    await adminResetPassword(resetPwdForm.value.id, resetPwdForm.value.newPassword)
    ElMessage.success('密码重置成功')
    resetPwdDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '重置失败')
  } finally {
    resetPwdLoading.value = false
  }
}

const handleDeleteUser = async (user: AdminUser) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await adminDeleteUser(user.id)
    ElMessage.success('删除成功')
    loadUserList()
    loadStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const loadAllCheckIns = async () => {
  checkInLoading.value = true
  try {
    const res = await getAllCheckIns()
    checkInList.value = res.data
  } catch (error) {
    ElMessage.error('加载打卡记录失败')
  } finally {
    checkInLoading.value = false
  }
}

const handleDeleteCheckIn = async (checkIn: CheckIn) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条打卡记录吗？\n地点：${checkIn.locationName}\n用户：${checkIn.username}`,
      '确认删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await adminDeleteCheckIn(checkIn.id)
    ElMessage.success('删除成功')
    loadAllCheckIns()
    loadStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const loadStats = async () => {
  try {
    const locationRes = await getAllLocations()
    stats.value.locationCount = locationRes.data.length

    const userRes = await getUserList(1, 1)
    stats.value.userCount = userRes.data.total

    const statsRes = await getCheckInStats()
    stats.value.checkInCount = statsRes.data.totalCheckIns
    stats.value.totalLikes = statsRes.data.totalLikes
    stats.value.todayCheckIn = statsRes.data.todayCheckIns
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const handleExport = (command: string) => {
  if (command === 'users') {
    const headers = ['ID', '用户名', '邮箱', '角色', '注册时间']
    const rows = userList.value.map(u => [u.id, u.username, u.email || '', u.role, formatTime(u.createdAt)])
    downloadCsv(headers, rows, '用户列表')
  } else if (command === 'checkins') {
    const headers = ['ID', '用户', '地点', '内容', '点赞数', '打卡时间']
    const rows = checkInList.value.map(c => [c.id, c.username, c.locationName, c.content || '', c.likeCount || 0, formatTime(c.checkInTime)])
    downloadCsv(headers, rows, '打卡记录')
  }
}

const downloadCsv = (headers: string[], rows: (string | number)[][], filename: string) => {
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
  ].join('\n')

  const BOM = '﻿'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${filename}_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(() => {
  if (!userStore.isAdmin) {
    ElMessage.error('无权访问')
    router.push('/')
    return
  }
  loadUserList()
  loadStats()
})
</script>

<style scoped>
.admin-table :deep(.el-table__row:hover) {
  background-color: #FFF5F8;
}
</style>

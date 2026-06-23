<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">店铺推荐审核</h2>

    <!-- 统计卡片 -->
    <div class="flex gap-4 mb-6">
      <el-card shadow="hover" class="flex-1 text-center">
        <div class="text-sm text-gray-500 mb-1">待审核数</div>
        <div class="text-2xl font-bold text-orange-500">{{ stats.pendingCount }}</div>
      </el-card>
      <el-card shadow="hover" class="flex-1 text-center">
        <div class="text-sm text-gray-500 mb-1">本周推荐数</div>
        <div class="text-2xl font-bold text-blue-500">{{ stats.weeklyCount }}</div>
      </el-card>
      <el-card shadow="hover" class="flex-1 text-center">
        <div class="text-sm text-gray-500 mb-1">宝藏店铺数</div>
        <div class="text-2xl font-bold text-yellow-500">{{ stats.featuredCount }}</div>
      </el-card>
    </div>

    <!-- 筛选栏 -->
    <div class="mb-4 flex gap-3">
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 150px" @change="loadData">
        <el-option label="全部" value="" />
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已驳回" :value="-1" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索店铺名称/推荐人"
        clearable
        style="width: 250px"
        @keyup.enter="loadData"
      />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="shopName" label="店铺名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="nickname" label="推荐人" width="120" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag :type="recommendStatusType(row.status)">{{ recommendStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="推荐时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="primary" @click="openAudit(row)">审核</el-button>
          <el-button v-if="row.status === 1" size="small" :type="row.isFeatured ? 'warning' : 'success'" @click="toggleFeature(row)">
            {{ row.isFeatured ? '取消宝藏' : '标记宝藏' }}
          </el-button>
          <el-button size="small" @click="openDetail(row)">详情</el-button>
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

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" title="审核推荐" width="500px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="-1">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="auditForm.status === -1" label="驳回原因">
          <el-input v-model="auditForm.reason" type="textarea" :rows="3" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="推荐详情" size="500px">
      <template v-if="detailRow">
        <div class="detail-section">
          <h4 class="detail-label">店铺名称</h4>
          <p>{{ detailRow.shopName }}</p>
        </div>
        <div class="detail-section">
          <h4 class="detail-label">分类</h4>
          <p>{{ detailRow.category }}</p>
        </div>
        <div class="detail-section">
          <h4 class="detail-label">城市 / 地址</h4>
          <p>{{ detailRow.city }} {{ detailRow.address }}</p>
        </div>
        <div class="detail-section">
          <h4 class="detail-label">推荐人</h4>
          <p>{{ detailRow.nickname }}</p>
        </div>
        <div class="detail-section">
          <h4 class="detail-label">推荐理由</h4>
          <p>{{ detailRow.reason }}</p>
        </div>
        <div class="detail-section">
          <h4 class="detail-label">位置坐标</h4>
          <p>{{ detailRow.latitude }}, {{ detailRow.longitude }}</p>
        </div>
        <div class="detail-section">
          <h4 class="detail-label">审核状态</h4>
          <p>
            <el-tag :type="recommendStatusType(detailRow.status)">{{ recommendStatusText(detailRow.status) }}</el-tag>
            <el-tag v-if="detailRow.isFeatured" type="warning" class="ml-2">宝藏店铺</el-tag>
          </p>
        </div>
        <div v-if="detailRow.auditReason" class="detail-section">
          <h4 class="detail-label">审核备注</h4>
          <p>{{ detailRow.auditReason }}</p>
        </div>
        <div class="detail-section">
          <h4 class="detail-label">推荐时间</h4>
          <p>{{ detailRow.createdAt }}</p>
        </div>
        <div v-if="detailRow.images && detailRow.images.length" class="detail-section">
          <h4 class="detail-label">图片</h4>
          <div class="flex gap-2 flex-wrap">
            <el-image
              v-for="(img, idx) in detailRow.images"
              :key="idx"
              :src="img"
              :preview-src-list="detailRow.images"
              :initial-index="idx"
              fit="cover"
              style="width: 100px; height: 100px; border-radius: 6px;"
            />
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getShopRecommendAdminList,
  adminAuditShopRecommend,
  adminFeatureShopRecommend,
  getShopRecommendAdminStats,
} from '@/api/shop-recommend'
import type { ShopRecommendItem, ShopRecommendStats } from '@/api/shop-recommend'

const statusFilter = ref<number | ''>('')
const keyword = ref('')
const tableData = ref<ShopRecommendItem[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const stats = reactive<ShopRecommendStats>({
  pendingCount: 0,
  weeklyCount: 0,
  featuredCount: 0,
})

const recommendStatusText = (s: number) => {
  if (s === 0) return '待审核'
  if (s === 1) return '已通过'
  if (s === -1) return '已驳回'
  return '未知'
}

const recommendStatusType = (s: number) => {
  if (s === 0) return 'warning'
  if (s === 1) return 'success'
  if (s === -1) return 'danger'
  return 'info'
}

const loadStats = async () => {
  try {
    const res = await getShopRecommendAdminStats()
    Object.assign(stats, res.data)
  } catch {
    // stats load failure is non-critical
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getShopRecommendAdminList({
      page: page.value,
      size: pageSize.value,
      status: statusFilter.value !== '' ? (statusFilter.value as number) : undefined,
      keyword: keyword.value || undefined,
    })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

// --- 审核 ---
const auditVisible = ref(false)
const auditForm = reactive({ id: 0, status: 1, reason: '' })

const openAudit = (row: ShopRecommendItem) => {
  auditForm.id = row.id
  auditForm.status = 1
  auditForm.reason = ''
  auditVisible.value = true
}

const submitAudit = async () => {
  if (auditForm.status === -1 && !auditForm.reason.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  await adminAuditShopRecommend(auditForm.id, {
    status: auditForm.status,
    reason: auditForm.reason || undefined,
  })
  ElMessage.success('审核完成')
  auditVisible.value = false
  loadData()
  loadStats()
}

// --- 标记宝藏 ---
const toggleFeature = async (row: ShopRecommendItem) => {
  const action = row.isFeatured ? '取消宝藏店铺标记' : '标记为宝藏店铺'
  try {
    await ElMessageBox.confirm(`确定要${action}吗？`, '提示', { type: 'warning' })
    await adminFeatureShopRecommend(row.id)
    ElMessage.success(`${action}成功`)
    loadData()
    loadStats()
  } catch {
    // cancelled
  }
}

// --- 详情 ---
const detailVisible = ref(false)
const detailRow = ref<ShopRecommendItem | null>(null)

const openDetail = (row: ShopRecommendItem) => {
  detailRow.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.detail-section {
  margin-bottom: 16px;
}

.detail-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 4px;
}

.detail-section p {
  margin: 0;
  color: #303133;
  line-height: 1.6;
}
</style>

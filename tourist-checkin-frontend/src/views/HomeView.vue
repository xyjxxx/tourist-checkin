<template>
  <div class="h-[calc(100vh-56px)] flex bg-[#F1F2F3]">
    <!-- Left Sidebar - Check-in List -->
    <div class="w-80 bg-white border-r border-[#E3E5E7] flex flex-col">
      <div class="p-5 border-b border-[#F1F2F3]">
        <h2 class="text-lg font-bold text-[#18191C]">我的足迹</h2>
        <p class="text-sm text-[#9499A0] mt-1">
          已打卡 <span class="font-semibold text-[#FB7299]">{{ checkInStore.checkIns.length }}</span> 个地点
        </p>
      </div>
      <div class="flex-1 overflow-y-auto p-3 space-y-2" v-loading="checkInStore.loading">
        <div
          v-for="item in checkInStore.checkIns"
          :key="item.id"
          class="p-3 rounded-lg cursor-pointer transition-all duration-200 border border-transparent hover:bg-[#F1F2F3]"
          :class="{
            'border-[#FB7299] bg-[#FFF0F3]': selectedCheckIn?.id === item.id
          }"
          @click="selectCheckIn(item)"
        >
          <div class="flex items-start gap-3">
            <el-avatar :size="40" :src="item.avatar || '/default-avatar.png'" class="cursor-pointer hover:ring-2 hover:ring-[#FB7299] transition-all" @click.stop="$router.push(`/profile/${item.userId}`)" />
            <div class="flex-1 min-w-0">
              <h3 class="font-semibold text-[#18191C] text-sm truncate">{{ item.locationName }}</h3>
              <el-tooltip :content="getFullDateTime(item.checkInTime)" placement="top">
                <p class="text-xs text-[#9499A0] mt-0.5">{{ formatRelativeTime(item.checkInTime) }}</p>
              </el-tooltip>
              <p v-if="item.content" class="text-sm text-[#61666D] mt-1.5 line-clamp-2 leading-relaxed">{{ item.content }}</p>
              <div v-if="item.images?.length" class="flex gap-1.5 mt-2">
                <el-image
                  v-for="(img, idx) in item.images.slice(0, 3)"
                  :key="idx"
                  :src="img"
                  class="w-14 h-14 rounded-lg object-cover"
                  :preview-src-list="item.images"
                  :initial-index="idx"
                />
              </div>
            </div>
          </div>
        </div>
        <el-empty v-if="!checkInStore.loading && checkInStore.checkIns.length === 0" description="还没有打卡记录，快去添加吧" :image-size="60" />
      </div>
    </div>

    <!-- Center Map -->
    <div class="flex-1 relative">
      <MapContainer
        ref="mapRef"
        :check-ins="checkInStore.checkIns"
        @marker-click="selectCheckIn"
        @map-click="handleMapClick"
      />

      <!-- FAB -->
      <el-button
        type="primary"
        size="large"
        class="absolute bottom-8 left-1/2 -translate-x-1/2 shadow-fab rounded-xl text-base font-semibold px-8 py-3 h-auto"
        :icon="Plus"
        @click="showCheckInDialog = true"
      >
        我要打卡
      </el-button>
    </div>

    <!-- Right Detail Panel -->
    <transition name="slide-panel">
      <div v-if="selectedCheckIn" class="w-96 bg-white border-l border-[#E3E5E7] overflow-y-auto shadow-lg">
        <div class="sticky top-0 bg-white/90 backdrop-blur-sm border-b border-[#E3E5E7] p-4 flex justify-between items-center z-10">
          <h2 class="text-lg font-bold text-[#18191C]">打卡详情</h2>
          <el-button type="danger" link :icon="Delete" @click="handleDelete(selectedCheckIn.id)" size="small">
            删除
          </el-button>
        </div>
        <div class="p-4">
          <div
            class="flex items-center gap-3 mb-4 cursor-pointer hover:opacity-80 transition-opacity"
            @click="$router.push(`/profile/${selectedCheckIn.userId}`)"
          >
            <el-avatar :size="48" :src="selectedCheckIn.avatar || '/default-avatar.png'" />
            <div>
              <p class="font-semibold text-[#18191C]">{{ selectedCheckIn.username }}</p>
              <p class="text-sm text-[#9499A0]">{{ formatDateSafe(selectedCheckIn.checkInTime, 'YYYY-MM-DD HH:mm:ss') }}</p>
              <p class="text-xs text-[#9499A0] mt-0.5">{{ formatRelativeTime(selectedCheckIn.checkInTime) }}</p>
            </div>
          </div>

          <div class="bg-[#F1F2F3] rounded-xl p-4 mb-4">
            <div class="flex items-center gap-2 mb-2">
              <svg class="w-4 h-4 text-[#FB7299]" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
              </svg>
              <h3 class="text-xl font-bold text-[#18191C]">{{ selectedCheckIn.locationName }}</h3>
            </div>
            <p class="text-[#61666D] leading-relaxed text-sm">{{ selectedCheckIn.content || '暂无描述' }}</p>
          </div>

          <div v-if="selectedCheckIn.images?.length" class="mb-4 space-y-2">
            <el-image
              v-for="(img, idx) in selectedCheckIn.images"
              :key="idx"
              :src="img"
              class="w-full rounded-xl object-cover"
              :preview-src-list="selectedCheckIn.images"
              :initial-index="idx"
            />
          </div>

          <div class="flex items-center gap-4 pt-4 border-t border-[#E3E5E7]">
            <el-button
              :type="selectedCheckIn.hasLiked ? 'danger' : 'default'"
              :icon="selectedCheckIn.hasLiked ? StarFilled : Star"
              @click="handleLike(selectedCheckIn.id)"
              round
            >
              {{ selectedCheckIn.likeCount || 0 }}
            </el-button>
          </div>

          <!-- Comments -->
          <div class="mt-4 pt-4 border-t border-[#E3E5E7]">
            <CommentSection :check-in-id="selectedCheckIn.id" :current-user-id="userStore.userInfo?.id || 0" />
          </div>
        </div>
      </div>
    </transition>

    <!-- Check-in Dialog -->
    <CheckInDialog v-model="showCheckInDialog" @success="onCheckInSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Star, StarFilled, Delete } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCheckInStore } from '@/stores/checkin'
import MapContainer from '@/components/MapContainer.vue'
import CheckInDialog from '@/components/CheckInDialog.vue'
import CommentSection from '@/components/CommentSection.vue'
import type { CheckIn } from '@/types'
import { formatDateSafe, formatRelativeTime, getFullDateTime } from '@/utils/date'

const router = useRouter()
const userStore = useUserStore()
const checkInStore = useCheckInStore()
const mapRef = ref<InstanceType<typeof MapContainer>>()
const selectedCheckIn = ref<CheckIn | null>(null)
const showCheckInDialog = ref(false)

const selectCheckIn = (checkIn: CheckIn) => {
  selectedCheckIn.value = checkIn
  mapRef.value?.centerToLocation(checkIn.longitude, checkIn.latitude)
}

const handleMapClick = (lng: number, lat: number) => {
  console.log('Map clicked:', lng, lat)
}

const handleLike = async (id: number) => {
  await checkInStore.toggleLike(id)
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条打卡记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await checkInStore.removeCheckIn(id)
    selectedCheckIn.value = null
    ElMessage.success('删除成功')
  } catch {
    // cancelled
  }
}

const onCheckInSuccess = async () => {
  await checkInStore.fetchMyCheckIns()
  ElMessage.success('打卡成功')
}

onMounted(() => {
  checkInStore.fetchMyCheckIns()
})
</script>

<style scoped>
.slide-panel-enter-active {
  transition: all 0.3s ease-out;
}

.slide-panel-leave-active {
  transition: all 0.2s ease-in;
}

.slide-panel-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.slide-panel-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>

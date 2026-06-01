<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-5xl mx-auto p-6">
      <!-- Search Results -->
      <section v-if="isSearching" class="mb-8 animate-fade-in">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h2 class="text-xl font-heading font-bold text-[#18191C]">搜索结果: "{{ searchQuery }}"</h2>
            <p class="text-sm text-[#9499A0] mt-0.5">找到 {{ searchResults.length }} 个地点</p>
          </div>
          <el-button text @click="isSearching = false; searchQuery = ''">清除搜索</el-button>
        </div>
        <div v-if="searchResults.length" class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div
            v-for="location in searchResults"
            :key="location.id"
            class="group cursor-pointer rounded-card overflow-hidden shadow-card hover:shadow-card-hover transition-all duration-300 hover:-translate-y-1"
          >
            <div class="relative aspect-[4/3] overflow-hidden">
              <img
                :src="location.coverImage || '/default-location.jpg'"
                :alt="location.name"
                loading="lazy"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
              />
              <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-black/10 to-transparent"></div>
              <div class="absolute bottom-3 left-3 right-3 text-white">
                <p class="font-semibold text-sm">{{ location.name }}</p>
                <p class="text-xs opacity-75">{{ location.city }}</p>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="没有找到匹配的地点" :image-size="80" />
      </section>

      <!-- Hot Locations -->
      <section v-if="!isSearching" class="mb-8 animate-fade-in">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h2 class="text-xl font-heading font-bold text-[#18191C]">热门打卡地点</h2>
            <p class="text-sm text-[#9499A0] mt-0.5">探索大家的旅行足迹</p>
          </div>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div
            v-for="location in hotLocations"
            :key="location.id"
            class="group cursor-pointer rounded-card overflow-hidden shadow-card hover:shadow-card-hover transition-all duration-300 hover:-translate-y-1"
          >
            <div class="relative aspect-[4/3] overflow-hidden">
              <img
                :src="location.coverImage || '/default-location.jpg'"
                :alt="location.name"
                loading="lazy"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
              />
              <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-black/10 to-transparent"></div>
              <div class="absolute bottom-3 left-3 right-3 text-white">
                <p class="font-semibold text-sm">{{ location.name }}</p>
                <p class="text-xs opacity-75">{{ location.city }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Trending Topics -->
      <section v-if="!isSearching" class="mb-8">
        <div class="card-elevated p-6">
          <TrendingTopics />
        </div>
      </section>

      <!-- Hot Travel Notes -->
      <section v-if="!isSearching" class="mb-8">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-heading font-bold text-[#18191C]">精选游记</h2>
          <el-button text type="primary" @click="$router.push('/travel-notes')">
            查看全部
            <el-icon class="ml-1"><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <TravelNoteCard v-for="note in travelNoteStore.hotNotes.slice(0, 3)" :key="note.id" :note="note" />
        </div>
      </section>

      <!-- Recent Check-ins -->
      <section v-loading="checkInStore.loading" class="card-elevated p-6">
        <h2 class="text-xl font-heading font-bold text-[#18191C] mb-4">最新动态</h2>

        <div v-if="!checkInStore.loading && checkInStore.recentCheckIns.length === 0" class="text-center py-12">
          <el-empty description="暂无动态" :image-size="80" />
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="item in checkInStore.recentCheckIns"
            :key="item.id"
            class="flex gap-4 p-4 rounded-card cursor-pointer transition-all duration-200 border border-transparent hover:bg-white hover:border-[#E3E5E7] hover:shadow-card-hover"
            @click="viewOnMap(item)"
          >
            <div class="relative">
              <el-avatar :size="48" :src="item.avatar || '/default-avatar.png'" class="ring-2 ring-[#FFC1D3]" />
              <div class="absolute -bottom-0.5 -right-0.5 w-4 h-4 bg-[#FF85AB] rounded-full border-2 border-white"></div>
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <div>
                  <p class="font-semibold text-[#18191C]">{{ item.username }}</p>
                  <p class="text-sm text-[#9499A0]">
                    打卡了
                    <span class="text-[#FB7299] font-medium">{{ item.locationName }}</span>
                  </p>
                </div>
                <el-tooltip :content="getFullDateTime(item.checkInTime)" placement="top">
                  <span class="text-xs text-[#9499A0] whitespace-nowrap">{{ formatRelativeTime(item.checkInTime) }}</span>
                </el-tooltip>
              </div>

              <p v-if="item.content" class="text-[#61666D] mt-2 text-sm leading-relaxed">{{ item.content }}</p>

              <div v-if="item.images?.length" class="flex gap-2 mt-2">
                <img
                  v-for="(img, idx) in item.images.slice(0, 4)"
                  :key="idx"
                  :src="img"
                  class="w-16 h-16 rounded-lg object-cover"
                />
              </div>

              <div class="flex items-center gap-4 mt-2">
                <span class="text-xs text-[#9499A0] flex items-center gap-1">
                  <el-icon :size="14"><View /></el-icon>
                  <Star class="w-3 h-3" />
                  {{ item.likeCount }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowRight, View, Star } from '@element-plus/icons-vue'
import { useCheckInStore } from '@/stores/checkin'
import { useTravelNoteStore } from '@/stores/travel-note'
import { getAllLocations } from '@/api/location'
import { ElMessage } from 'element-plus'
import TrendingTopics from '@/components/TrendingTopics.vue'
import TravelNoteCard from '@/components/TravelNoteCard.vue'
import type { Location, CheckIn } from '@/types'
import { formatRelativeTime, getFullDateTime } from '@/utils/date'

const router = useRouter()
const route = useRoute()
const checkInStore = useCheckInStore()
const travelNoteStore = useTravelNoteStore()
const hotLocations = ref<Location[]>([])
const searchQuery = ref('')
const searchResults = ref<Location[]>([])
const isSearching = ref(false)

const viewOnMap = (item: CheckIn) => {
  router.push(`/profile/${item.userId}`)
}

const performSearch = (q: string) => {
  searchQuery.value = q
  if (!q.trim()) {
    searchResults.value = []
    isSearching.value = false
    return
  }
  isSearching.value = true
  const lowerQ = q.toLowerCase()
  searchResults.value = hotLocations.value.filter(
    loc => loc.name?.toLowerCase().includes(lowerQ) || loc.city?.toLowerCase().includes(lowerQ)
  )
}

watch(() => route.query.q, (newQ) => {
  if (newQ) {
    performSearch(String(newQ))
  }
}, { immediate: true })

onMounted(async () => {
  checkInStore.fetchRecentCheckIns()
  travelNoteStore.fetchHot()
  try {
    const res = await getAllLocations()
    hotLocations.value = res.data.slice(0, 8)
    // 如果有搜索查询，加载后再搜索
    if (route.query.q) {
      performSearch(String(route.query.q))
    }
  } catch {
    ElMessage.error('加载热门地点失败')
  }
})
</script>

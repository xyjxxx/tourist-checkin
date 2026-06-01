<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-4xl mx-auto p-4">
      <el-select v-model="year" class="mb-4" @change="loadData">
        <el-option v-for="y in years" :key="y" :label="`${y}年`" :value="y" />
      </el-select>

      <div v-if="report" class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div class="card-elevated p-5 text-center relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#FF85AB] to-[#FB7299]"></div>
          <div class="text-2xl font-extrabold text-[#FB7299] mt-2">{{ report.totalCheckIns }}</div>
          <div class="text-sm text-[#9499A0] mt-1">总打卡</div>
        </div>
        <div class="card-elevated p-5 text-center relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-green-400 to-green-600"></div>
          <div class="text-2xl font-extrabold text-green-600 mt-2">{{ report.totalCities }}</div>
          <div class="text-sm text-[#9499A0] mt-1">覆盖城市</div>
        </div>
        <div class="card-elevated p-5 text-center relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-purple-400 to-purple-600"></div>
          <div class="text-2xl font-extrabold text-purple-600 mt-2">{{ report.totalLikes }}</div>
          <div class="text-sm text-[#9499A0] mt-1">获赞</div>
        </div>
        <div class="card-elevated p-5 text-center relative overflow-hidden">
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-orange-400 to-orange-600"></div>
          <div class="text-2xl font-extrabold text-orange-600 mt-2">{{ report.longestStreak }}</div>
          <div class="text-sm text-[#9499A0] mt-1">最长连续(天)</div>
        </div>
      </div>

      <div v-if="report" class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <div class="card-elevated p-5">
          <h4 class="text-sm font-bold text-[#18191C] mb-3">活跃偏好</h4>
          <div class="space-y-2">
            <div class="flex justify-between text-sm">
              <span class="text-[#9499A0]">最活跃月份</span>
              <span class="font-semibold text-[#18191C]">{{ report.mostActiveMonth }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-[#9499A0]">最活跃城市</span>
              <span class="font-semibold text-[#18191C]">{{ report.mostActiveCity }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-[#9499A0]">最喜欢类型</span>
              <span class="font-semibold text-[#18191C]">{{ report.favoriteCategory }}</span>
            </div>
          </div>
        </div>
        <div class="card-elevated p-5">
          <h4 class="text-sm font-bold text-[#18191C] mb-3">月度趋势</h4>
          <div v-if="trend" style="height: 180px">
            <div class="flex items-end gap-1 h-full">
              <div
                v-for="(val, i) in trend.values" :key="i"
                class="flex-1 rounded-t transition-all hover:opacity-80"
                :style="{
                  height: maxVal ? (val / maxVal * 100) + '%' : '0%',
                  background: `linear-gradient(to top, #FB7299, #FF85AB)`,
                  minHeight: val > 0 ? '4px' : '0'
                }"
                :title="`${trend.labels[i]}: ${val}`"
              />
            </div>
          </div>
          <div v-if="trend" class="flex justify-between mt-2">
            <span v-for="(label, i) in trend.labels.slice(0, 12)" :key="i" class="text-xs text-[#9499A0]">{{ label }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useStatisticsStore } from '@/stores/statistics'

const store = useStatisticsStore()
const year = ref(2026)
const years = [2024, 2025, 2026]

const report = computed(() => store.report)
const trend = computed(() => store.trend)
const maxVal = computed(() => trend.value ? Math.max(...trend.value.values, 1) : 1)

const loadData = () => { store.fetchReport(year.value); store.fetchTrend(year.value) }
onMounted(loadData)
</script>

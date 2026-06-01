<template>
  <div>
    <el-button :size="size" circle :icon="Share" @click="showDialog = true" />
    <el-dialog v-model="showDialog" title="分享" width="440px" center class="rounded-2xl">
      <div class="text-center" v-if="poster">
        <div class="poster-preview bg-[#F1F2F3] rounded-xl p-5 mb-4 border border-[#E3E5E7]">
          <h4 class="font-bold text-[#18191C]">{{ poster.locationName }}</h4>
          <p class="text-sm text-[#61666D] mt-1">{{ poster.content }}</p>
          <el-image v-if="poster.imageUrl" :src="poster.imageUrl" fit="cover" class="w-full h-36 mt-3 rounded-lg" />
          <img v-if="poster.qrCode" :src="poster.qrCode" class="w-24 h-24 mx-auto mt-3" alt="QR" />
          <p class="text-xs text-[#9499A0] mt-2">{{ poster.checkInTime }}</p>
        </div>
      </div>
      <div v-else class="text-center py-8">
        <el-icon class="is-loading text-[#FB7299]" :size="32"><Loading /></el-icon>
        <p class="text-sm text-[#9499A0] mt-2">生成海报中...</p>
      </div>
      <template #footer>
        <el-button @click="showDialog = false" round>关闭</el-button>
        <el-button type="primary" @click="copyLink" round>复制链接</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Share, Loading } from '@element-plus/icons-vue'
import { generatePoster, getShareLink, type PosterData } from '@/api/share'
import { ElMessage } from 'element-plus'

const props = defineProps<{ checkInId: number; size?: 'small' | 'default' | 'large' }>()

const showDialog = ref(false)
const poster = ref<PosterData | null>(null)

watch(showDialog, async (val) => {
  if (val && !poster.value) {
    try {
      const res = await generatePoster(props.checkInId)
      poster.value = (res.data as any) || res.data
    } catch { poster.value = null }
  }
})

const copyLink = async () => {
  try {
    const res = await getShareLink(props.checkInId)
    await navigator.clipboard.writeText(res.data as string)
    ElMessage.success('链接已复制到剪贴板')
  } catch { ElMessage.error('复制失败') }
}
</script>

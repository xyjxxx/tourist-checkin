<template>
  <div class="min-h-screen">
    <div class="max-w-3xl mx-auto p-4">
      <div class="card-elevated p-6">
        <el-form :model="form" label-position="top">
          <el-form-item label="标题" required>
            <el-input v-model="form.title" placeholder="游记标题" maxlength="100" show-word-limit size="large" />
          </el-form-item>
          <el-form-item label="城市">
            <el-input v-model="form.city" placeholder="旅行城市" size="large" />
          </el-form-item>
          <el-form-item label="封面图">
            <div class="flex items-start gap-4">
              <!-- 上传区域 -->
              <div
                v-if="!coverPreview"
                class="w-40 h-28 border-2 border-dashed border-[#E3E5E7] rounded-xl flex flex-col items-center justify-center cursor-pointer hover:border-[#FB7299] hover:bg-[#FFF0F3] transition-all"
                @click="triggerCoverUpload"
              >
                <svg class="w-8 h-8 text-[#FFC1D3] mb-1" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
                  <path d="M12 16V8M8 12h8M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/>
                  <circle cx="12" cy="13" r="3"/>
                </svg>
                <span class="text-xs text-[#9499A0]">点击上传封面</span>
              </div>
              <!-- 预览区域 -->
              <div v-else class="relative group">
                <img :src="coverPreview" class="w-40 h-28 rounded-xl object-cover border border-[#E3E5E7]" />
                <div class="absolute inset-0 rounded-xl bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                  <button class="w-8 h-8 rounded-full bg-white/90 flex items-center justify-center hover:bg-white transition-colors" @click="triggerCoverUpload" title="更换">
                    <svg class="w-4 h-4 text-[#61666D]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button class="w-8 h-8 rounded-full bg-white/90 flex items-center justify-center hover:bg-white transition-colors" @click="removeCover" title="删除">
                    <svg class="w-4 h-4 text-red-500" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                  </button>
                </div>
                <div v-if="coverUploading" class="absolute inset-0 rounded-xl bg-black/50 flex items-center justify-center">
                  <svg class="w-6 h-6 text-white animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10" stroke-opacity="0.3"/><path d="M12 2a10 10 0 019.95 9" stroke-linecap="round"/></svg>
                </div>
              </div>
              <div class="text-xs text-[#9499A0] pt-2">
                <p>支持 JPG、PNG、GIF、WEBP 格式</p>
                <p>建议尺寸 16:9，最大 10MB</p>
              </div>
            </div>
            <input ref="coverInputRef" type="file" accept="image/*" class="hidden" @change="handleCoverChange" />
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="form.tags" multiple filterable allow-create placeholder="输入标签，回车添加" style="width: 100%" />
          </el-form-item>
          <el-form-item label="内容" required>
            <el-input v-model="form.content" type="textarea" :rows="12" placeholder="写下你的旅行故事..." />
          </el-form-item>
          <el-form-item>
            <div class="flex gap-3">
              <el-button type="primary" size="large" :loading="loading" :disabled="!form.title || !form.content" @click="submit">
                发布游记
              </el-button>
              <el-button size="large" @click="$router.back()">取消</el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createTravelNote } from '@/api/travel-note'
import { uploadFiles } from '@/api/file'
import { compressImage } from '@/utils/image-compress'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const coverInputRef = ref<HTMLInputElement>()
const coverPreview = ref('')
const coverUploading = ref(false)
let coverFile: File | null = null

const form = reactive({
  title: '', city: '', coverImage: '',
  tags: [] as string[], content: ''
})

const triggerCoverUpload = () => {
  coverInputRef.value?.click()
}

const handleCoverChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB')
    return
  }

  coverFile = file
  // 本地预览
  coverPreview.value = URL.createObjectURL(file)

  // 清除input值允许重新选择同一文件
  if (coverInputRef.value) coverInputRef.value.value = ''
}

const removeCover = () => {
  coverFile = null
  coverPreview.value = ''
  form.coverImage = ''
}

const submit = async () => {
  loading.value = true
  try {
    // 如果有封面图文件，先压缩再上传
    if (coverFile) {
      coverUploading.value = true
      const compressed = await compressImage(coverFile, {
        maxWidth: 1200,
        maxHeight: 675,
        quality: 0.85,
        maxSizeKB: 500
      })
      const uploadRes = await uploadFiles([compressed])
      form.coverImage = uploadRes.data.urls[0]
      coverUploading.value = false
    }

    const res = await createTravelNote(form as any)
    ElMessage.success('发布成功')
    router.push(`/travel-notes/${(res.data as any).id}`)
  } catch (error: any) {
    ElMessage.error(error?.message || '发布失败，请重试')
  } finally {
    loading.value = false
    coverUploading.value = false
  }
}
</script>

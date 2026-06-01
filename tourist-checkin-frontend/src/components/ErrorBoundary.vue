<template>
  <div v-if="hasError" class="min-h-screen flex items-center justify-center bg-[#F1F2F3]">
    <div class="bg-white rounded-2xl shadow-dialog p-8 max-w-md w-full text-center border border-[#E3E5E7]">
      <div class="w-20 h-20 mx-auto mb-4 rounded-full bg-red-100 flex items-center justify-center">
        <svg class="w-10 h-10 text-red-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
        </svg>
      </div>
      <h1 class="text-xl font-bold text-[#18191C] mb-2">出错了</h1>
      <p class="text-[#9499A0] mb-6">{{ errorMessage }}</p>
      <div class="flex gap-3 justify-center">
        <el-button type="primary" @click="reloadPage" round>刷新页面</el-button>
        <el-button @click="goHome" round>返回首页</el-button>
      </div>
    </div>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
import { ref, onErrorCaptured, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const hasError = ref(false)
const errorMessage = ref('页面发生错误')

const globalErrorHandler = (event: ErrorEvent) => {
  console.error('Global error:', event.error)
  ElMessage.error('发生错误: ' + (event.error?.message || '未知错误'))
}

const unhandledRejectionHandler = (event: PromiseRejectionEvent) => {
  console.error('Unhandled promise rejection:', event.reason)
  ElMessage.error('请求失败: ' + (event.reason?.message || '未知错误'))
}

onErrorCaptured((err: any) => {
  hasError.value = true
  errorMessage.value = err?.message || '未知错误'
  console.error('ErrorBoundary captured:', err)
  return false
})

onMounted(() => {
  window.addEventListener('error', globalErrorHandler)
  window.addEventListener('unhandledrejection', unhandledRejectionHandler)
})

onUnmounted(() => {
  window.removeEventListener('error', globalErrorHandler)
  window.removeEventListener('unhandledrejection', unhandledRejectionHandler)
})

const reloadPage = () => {
  window.location.reload()
}

const goHome = () => {
  hasError.value = false
  router.push('/')
}
</script>

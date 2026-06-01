<template>
  <div class="min-h-screen flex items-center justify-center bg-[#F1F2F3]">
    <div class="text-center">
      <div class="text-8xl font-extrabold text-[#FB7299]/20 mb-4">404</div>
      <h1 class="text-2xl font-bold text-[#18191C] mb-2">页面不存在</h1>
      <p class="text-[#9499A0] mb-8">你访问的页面已走失在旅途中...</p>
      <el-button type="primary" round @click="router.push('/')">
        <el-icon class="mr-1"><HomeFilled /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { HomeFilled } from '@element-plus/icons-vue'

const router = useRouter()
</script>

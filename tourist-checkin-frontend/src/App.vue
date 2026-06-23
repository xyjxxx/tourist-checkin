<template>
  <ErrorBoundary>
    <router-view />
  </ErrorBoundary>
</template>

<script setup lang="ts">
import ErrorBoundary from '@/components/ErrorBoundary.vue'
import { useDarkMode } from '@/composables/useDarkMode'
import { watch } from 'vue'

const { isDark } = useDarkMode()
watch(isDark, (dark) => {
  document.documentElement.classList.toggle('dark', dark)
}, { immediate: true })
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  font-family: system-ui, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background-color: #F1F2F3;
  color: #18191C;
  -webkit-font-smoothing: antialiased;
}
.dark body, .dark { background-color: #1a1a2e; color: #e0e0e0; }
#app { width: 100%; min-height: 100vh; }
</style>

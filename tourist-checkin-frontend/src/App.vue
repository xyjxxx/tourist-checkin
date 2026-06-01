<template>
  <ErrorBoundary>
    <!-- Login page: no navbar -->
    <template v-if="route.path === '/login'">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </template>

    <!-- Authenticated pages: persistent navbar + content -->
    <template v-else>
      <AppNavbar />
      <main class="pt-14 min-h-screen bg-[#F1F2F3]">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </template>
  </ErrorBoundary>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { watch } from 'vue'
import ErrorBoundary from '@/components/ErrorBoundary.vue'
import AppNavbar from '@/components/AppNavbar.vue'
import { useDarkMode } from '@/composables/useDarkMode'

const route = useRoute()
const { isDark } = useDarkMode()

watch(isDark, (dark) => {
  document.documentElement.classList.toggle('dark', dark)
}, { immediate: true })
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: system-ui, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background-color: #F1F2F3;
  color: #18191C;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.dark body, .dark {
  background-color: #1a1a2e;
  color: #e0e0e0;
}

#app {
  width: 100%;
  min-height: 100vh;
}

/* Page transition */
.page-enter-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>

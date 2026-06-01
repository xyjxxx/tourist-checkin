<template>
  <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99" class="notification-bell">
    <el-button circle :icon="Bell" @click="$router.push('/notifications')" />
  </el-badge>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { useNotificationStore } from '@/stores/notification'

const store = useNotificationStore()
const unreadCount = computed(() => store.unreadCount)
</script>

<style scoped>
.notification-bell :deep(.el-badge__content) {
  background: #EA580C;
  border: 2px solid white;
}
</style>

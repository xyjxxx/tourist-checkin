<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-2xl mx-auto p-4">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold text-[#18191C]">全部通知</h2>
        <el-button size="small" text type="primary" @click="store.handleMarkAllRead">全部已读</el-button>
      </div>

      <div v-if="store.notifications.length" class="space-y-2">
        <div
          v-for="item in store.notifications" :key="item.id"
          class="p-4 rounded-xl border transition-all duration-200 cursor-pointer"
          :class="{
            'bg-[#FFF0F3] border-[#FFC1D3] shadow-sm': !item.isRead,
            'bg-white border-transparent': item.isRead
          }"
          @click="store.handleMarkRead(item.id)"
        >
          <div class="flex items-start gap-3">
            <el-avatar :size="36" :src="item.fromUser?.avatar" class="ring-2 ring-[#FFD6E0]" />
            <div class="flex-1">
              <p class="text-sm text-[#18191C]">{{ item.content }}</p>
              <span class="text-xs text-[#9499A0] mt-1">{{ item.createdAt }}</span>
            </div>
            <div v-if="!item.isRead" class="w-2.5 h-2.5 bg-[#FB7299] rounded-full mt-2 flex-shrink-0 ring-2 ring-[#FFD6E0]" />
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无通知" :image-size="60" />

      <div class="flex justify-center mt-4">
        <el-pagination
          v-if="store.notifications.length >= 20"
          layout="prev, next"
          :total="50"
          @current-change="store.fetchNotifications"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useNotificationStore } from '@/stores/notification'

const store = useNotificationStore()
onMounted(() => { store.fetchNotifications(); store.fetchUnreadCount() })
</script>

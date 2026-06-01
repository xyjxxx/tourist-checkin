import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getNotifications, markRead, markAllRead, getUnreadCount } from '@/api/notification'
import { onMessage } from '@/utils/websocket'
import type { NotificationItem } from '@/types'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<NotificationItem[]>([])
  const unreadCount = ref(0)
  const error = ref<string | null>(null)

  const fetchNotifications = async () => {
    try {
      error.value = null
      const res = await getNotifications()
      notifications.value = res.data.list || res.data
      fetchUnreadCount()
      return res
    } catch (e: any) {
      error.value = e?.message || '获取通知失败'
    }
  }

  const fetchUnreadCount = async () => {
    try {
      const res = await getUnreadCount()
      unreadCount.value = res.data || 0
    } catch { /* 未读数获取失败，不影响主流程 */ }
  }

  const handleMarkRead = async (id: number) => {
    await markRead(id)
    const item = notifications.value.find(n => n.id === id)
    if (item) item.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }

  const handleMarkAllRead = async () => {
    await markAllRead()
    notifications.value.forEach(n => n.isRead = true)
    unreadCount.value = 0
  }

  // WebSocket 实时通知
  onMessage('notification', (data: NotificationItem) => {
    notifications.value.unshift(data)
    unreadCount.value++
  })

  return {
    notifications, unreadCount, error,
    fetchNotifications, fetchUnreadCount,
    handleMarkRead, handleMarkAllRead
  }
})

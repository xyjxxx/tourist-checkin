import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getHotTravelNotes, getRecentTravelNotes, getUserTravelNotes, likeTravelNote, collectTravelNote } from '@/api/travel-note'
import type { TravelNote } from '@/types'

export const useTravelNoteStore = defineStore('travel-note', () => {
  const hotNotes = ref<TravelNote[]>([])
  const recentNotes = ref<TravelNote[]>([])
  const userNotes = ref<Record<number, TravelNote[]>>({})
  const error = ref<string | null>(null)

  const fetchHot = async (limit = 10) => {
    try {
      error.value = null
      const res = await getHotTravelNotes(limit)
      hotNotes.value = res.data
    } catch (e: any) {
      error.value = e?.message || '获取热门游记失败'
    }
  }

  const fetchRecent = async (page = 1, size = 10) => {
    try {
      error.value = null
      const res = await getRecentTravelNotes(page, size)
      recentNotes.value = res.data
    } catch (e: any) {
      error.value = e?.message || '获取最新游记失败'
    }
  }

  const fetchByUser = async (userId: number) => {
    try {
      const res = await getUserTravelNotes(userId)
      userNotes.value[userId] = res.data
    } catch { /* 用户游记获取失败，不影响主流程 */ }
  }

  const handleLike = async (noteId: number) => {
    await likeTravelNote(noteId)
    const updateIn = (list: TravelNote[]) => {
      const n = list.find(x => x.id === noteId)
      if (n) { n.hasLiked = !n.hasLiked; n.likeCount += n.hasLiked ? 1 : -1 }
    }
    updateIn(hotNotes.value); updateIn(recentNotes.value)
    Object.values(userNotes.value).forEach(updateIn)
  }

  const handleCollect = async (noteId: number) => {
    await collectTravelNote(noteId)
    const updateIn = (list: TravelNote[]) => {
      const n = list.find(x => x.id === noteId)
      if (n) { n.hasCollected = !n.hasCollected; n.collectCount += n.hasCollected ? 1 : -1 }
    }
    updateIn(hotNotes.value); updateIn(recentNotes.value)
    Object.values(userNotes.value).forEach(updateIn)
  }

  return {
    hotNotes, recentNotes, userNotes, error,
    fetchHot, fetchRecent, fetchByUser, handleLike, handleCollect
  }
})

import request from './request'
import type { ApiResponse, TravelNote } from '@/types'

export const createTravelNote = (data: {
  title: string; content: string; coverImage?: string; city?: string
  tags?: string[]; images?: string[]; checkInPointIds?: number[]
}): Promise<ApiResponse<TravelNote>> =>
  request.post('/travel-note', data)

export const getTravelNoteDetail = (id: number): Promise<ApiResponse<TravelNote>> =>
  request.get(`/travel-note/${id}`)

export const getUserTravelNotes = (userId: number): Promise<ApiResponse<TravelNote[]>> =>
  request.get(`/travel-note/user/${userId}`)

export const getHotTravelNotes = (limit = 10): Promise<ApiResponse<TravelNote[]>> =>
  request.get('/travel-note/hot', { params: { limit } })

export const getRecentTravelNotes = (page = 1, size = 10): Promise<ApiResponse<TravelNote[]>> =>
  request.get('/travel-note/recent', { params: { page, size } })

export const likeTravelNote = (id: number): Promise<ApiResponse<void>> =>
  request.post(`/travel-note/${id}/like`)

export const collectTravelNote = (id: number): Promise<ApiResponse<void>> =>
  request.post(`/travel-note/${id}/collect`)

export const getLikedTravelNotes = (): Promise<ApiResponse<TravelNote[]>> =>
  request.get('/travel-note/liked')

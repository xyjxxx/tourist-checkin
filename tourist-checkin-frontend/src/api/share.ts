import request from './request'
import type { ApiResponse } from '@/types'

export interface PosterData {
  checkInId: number; locationName: string; content?: string
  imageUrl?: string; qrCode?: string; checkInTime: string
}

export const generatePoster = (checkInId: number, template = 'default'): Promise<ApiResponse<PosterData>> =>
  request.post('/share/poster', { checkInId, template })

export const getShareLink = (checkInId: number): Promise<ApiResponse<string>> =>
  request.get(`/share/link/${checkInId}`)

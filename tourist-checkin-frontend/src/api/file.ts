import request from './request'
import type { ApiResponse } from '@/types'

export const uploadFile = (file: File): Promise<ApiResponse<{ url: string }>> => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const uploadFiles = (files: File[]): Promise<ApiResponse<{ urls: string[] }>> => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return request.post('/file/upload/batch', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

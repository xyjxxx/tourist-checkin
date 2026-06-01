import request from './request'
import type { Location, ApiResponse } from '@/types'

export const getNearbyLocations = (
  lng: number,
  lat: number,
  radius = 5000
): Promise<ApiResponse<Location[]>> => {
  return request.get('/location/nearby', {
    params: { lng, lat, radius }
  })
}

export const getLocationsByCity = (city: string): Promise<ApiResponse<Location[]>> => {
  return request.get(`/location/city/${city}`)
}

export const getAllLocations = (): Promise<ApiResponse<Location[]>> => {
  return request.get('/location')
}

export const getLocationById = (id: number): Promise<ApiResponse<Location>> => {
  return request.get(`/location/${id}`)
}

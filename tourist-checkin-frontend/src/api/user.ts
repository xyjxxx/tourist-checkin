import request from './request'
import type { LoginForm, RegisterForm, UserInfo, ApiResponse, AdminUserList } from '@/types'

export const login = (data: LoginForm): Promise<ApiResponse<{ token: string }>> => {
  return request.post('/user/login', data)
}

export const register = (data: RegisterForm): Promise<ApiResponse<{ token: string }>> => {
  return request.post('/user/register', data)
}

export const getUserInfo = (): Promise<ApiResponse<UserInfo>> => {
  return request.get('/user/info')
}

/**
 * 获取其他用户的公开资料
 */
export const getUserProfile = (userId: number): Promise<ApiResponse<UserInfo>> => {
  return request.get(`/user/${userId}`)
}

/**
 * 注销账号
 */
export const deleteAccount = (password: string): Promise<ApiResponse<void>> => {
  return request.post('/user/delete-account', { password })
}

/**
 * 找回/重置密码
 */
export const forgotPassword = (data: {
  account: string
  email: string
  newPassword: string
}): Promise<ApiResponse<void>> => {
  return request.post('/user/forgot-password', data)
}

/**
 * 更新头像
 */
export const updateAvatar = (avatarUrl: string): Promise<ApiResponse<string>> => {
  return request.put('/user/avatar', { avatarUrl })
}

/**
 * 修改昵称
 */
export const updateUsername = (username: string): Promise<ApiResponse<void>> => {
  return request.put('/user/username', { username })
}

/**
 * 更新背景图
 */
export const updateBackground = (imageUrl: string): Promise<ApiResponse<string>> => {
  return request.put('/user/background', { imageUrl })
}

// ==================== 管理员接口 ====================

/**
 * 获取用户列表（管理员）
 */
export const getUserList = (
  page: number = 1,
  size: number = 10,
  keyword?: string,
  loginType?: string,
  role?: string
): Promise<ApiResponse<AdminUserList>> => {
  return request.get('/user/admin/list', {
    params: { page, size, keyword, loginType, role }
  })
}

/**
 * 管理员删除用户
 */
export const adminDeleteUser = (userId: number): Promise<ApiResponse<void>> => {
  return request.delete(`/user/admin/${userId}`)
}

/**
 * 管理员编辑用户
 */
export const adminUpdateUser = (
  userId: number,
  data: { email?: string; role?: string }
): Promise<ApiResponse<void>> => {
  return request.put(`/user/admin/${userId}`, data)
}

/**
 * 管理员重置用户密码
 */
export const adminResetPassword = (
  userId: number,
  newPassword: string
): Promise<ApiResponse<void>> => {
  return request.post(`/user/admin/${userId}/reset-password`, { newPassword })
}

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo, deleteAccount } from '@/api/user'
import type { UserInfo, LoginForm, RegisterForm } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(sessionStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  // 判断是否为管理员（包括超级管理员）
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN' || userInfo.value?.role === 'SUPER_ADMIN')
  const isSuperAdmin = computed(() => userInfo.value?.role === 'SUPER_ADMIN')

  const setToken = (newToken: string) => {
    token.value = newToken
    sessionStorage.setItem('token', newToken)
  }

  const clearToken = () => {
    token.value = ''
    userInfo.value = null
    sessionStorage.removeItem('token')
  }

  const loginAction = async (form: LoginForm) => {
    const res = await login(form)
    setToken(res.data.token)
    await fetchUserInfo()
    return res
  }

  const registerAction = async (form: RegisterForm) => {
    const res = await register(form)
    setToken(res.data.token)
    await fetchUserInfo()
    return res
  }

  const fetchUserInfo = async () => {
    const res = await getUserInfo()
    userInfo.value = res.data
    return res
  }

  const logout = () => {
    clearToken()
  }

  // 注销账号
  const deleteAccountAction = async (password: string) => {
    await deleteAccount(password)
    clearToken()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    isSuperAdmin,
    setToken,
    clearToken,
    loginAction,
    registerAction,
    fetchUserInfo,
    logout,
    deleteAccountAction
  }
})

<template>
  <div class="min-h-screen flex items-center justify-center bg-[#F5F5F7] relative overflow-hidden">
    <!-- Background decoration -->
    <div class="absolute inset-0 pointer-events-none">
      <div class="absolute top-[-10%] right-[-5%] w-[500px] h-[500px] bg-primary-500/5 rounded-full blur-[100px]"></div>
      <div class="absolute bottom-[-15%] left-[-10%] w-[600px] h-[600px] bg-primary-500/3 rounded-full blur-[120px]"></div>
    </div>

    <div class="relative z-10 w-full max-w-md px-4 animate-fade-in">
      <!-- Brand -->
      <div class="text-center mb-10">
        <div class="inline-flex items-center justify-center w-18 h-18 rounded-2xl bg-gradient-to-br from-primary-500 to-primary-700 mb-5 shadow-fab
                    transition-transform duration-200 hover:scale-105">
          <svg class="w-9 h-9 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="10" r="3"/>
            <path d="M12 2a8 8 0 0 0-8 8c0 5.4 8 12 8 12s8-6.6 8-12a8 8 0 0 0-8-8z"/>
          </svg>
        </div>
        <h1 class="text-4xl font-extrabold text-[#1D1D1F] tracking-tight">拾光旅记</h1>
        <p class="text-[#86868B] mt-2 text-sm tracking-wide">小程序管理后台</p>
      </div>

      <!-- Login Card -->
      <div class="bg-white rounded-3xl shadow-[0_8px_40px_-8px_rgba(0,0,0,0.1)] p-8">
        <h2 class="text-xl font-bold text-center mb-8 text-[#1D1D1F]">管理员登录</h2>
        <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" @keyup.enter="handleLogin">
          <el-form-item prop="account">
            <el-input v-model="loginForm.account" placeholder="管理员账号" size="large" :prefix-icon="User" class="login-input" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" :prefix-icon="Lock" show-password class="login-input" />
          </el-form-item>
          <el-button type="primary" size="large" class="w-full mt-3 login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form>
      </div>

      <p class="text-center text-[#AEAEB2] text-xs mt-8 tracking-wide">
        仅管理员可访问 · 小程序用户请在微信中使用
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loginFormRef = ref<FormInstance>()

const loginForm = reactive({ account: '', password: '' })

const loginRules: FormRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.loginAction(loginForm)
    if (!userStore.isAdmin) {
      ElMessage.error('该账号不是管理员，无法访问管理后台')
      userStore.logout()
      return
    }
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch {
    // 错误已在 request 拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px #E8E8ED;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  background: #F9F9FB;
  height: 48px;
}
.login-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #FFC1D3;
  background: #FFFFFF;
}
.login-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #FB7299;
  background: #FFFFFF;
}
.login-btn {
  border-radius: 12px;
  font-weight: 600;
  font-size: 16px;
  height: 48px;
  background: linear-gradient(135deg, #FB7299, #E85D88);
  border: none;
  box-shadow: 0 4px 14px rgba(251, 114, 153, 0.35);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 2px;
}
.login-btn:hover {
  box-shadow: 0 8px 24px rgba(251, 114, 153, 0.45);
  transform: translateY(-2px);
}
.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(251, 114, 153, 0.3);
}
</style>

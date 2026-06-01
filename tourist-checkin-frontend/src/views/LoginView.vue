<template>
  <div class="min-h-screen flex items-center justify-center bg-[#F1F2F3] relative overflow-hidden">
    <!-- Background decoration -->
    <div class="absolute inset-0 pointer-events-none">
      <div class="absolute top-10 left-10 w-64 h-64 bg-[#FB7299]/5 rounded-full blur-3xl"></div>
      <div class="absolute bottom-10 right-10 w-96 h-96 bg-[#FB7299]/3 rounded-full blur-3xl"></div>
      <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-[#FB7299]/3 rounded-full blur-3xl"></div>
    </div>

    <div class="relative z-10 w-full max-w-md px-4">
      <!-- Brand -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-[#FB7299] to-[#E85D88] mb-4 shadow-[0_8px_24px_rgba(251,114,153,0.3)]">
          <svg class="w-8 h-8 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="10" r="3"/>
            <path d="M12 2a8 8 0 0 0-8 8c0 5.4 8 12 8 12s8-6.6 8-12a8 8 0 0 0-8-8z"/>
          </svg>
        </div>
        <h1 class="text-4xl font-extrabold text-[#18191C] tracking-tight">
          拾光旅记
        </h1>
        <p class="text-[#9499A0] mt-2 text-sm">每一次出发，都值得被铭记</p>
      </div>

      <!-- Card -->
      <div class="bg-white rounded-2xl shadow-[0_8px_32px_rgba(0,0,0,0.08)] p-8">
        <el-tabs v-model="activeTab" class="login-tabs" stretch>
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" @keyup.enter="handleLogin">
              <el-form-item prop="account">
                <el-input
                  v-model="loginForm.account"
                  placeholder="请输入账号"
                  size="large"
                  :prefix-icon="User"
                  class="login-input"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                  class="login-input"
                />
              </el-form-item>
              <div class="flex justify-end mb-2">
                <el-button link type="primary" class="text-sm" @click="showForgotDialog = true">
                  忘记密码？
                </el-button>
              </div>
              <el-button
                type="primary"
                size="large"
                class="w-full mt-2 login-btn"
                :loading="loading"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" @keyup.enter="handleRegister">
              <el-form-item prop="account">
                <el-input
                  v-model="registerForm.account"
                  placeholder="请输入账号（用于登录）"
                  size="large"
                  :prefix-icon="User"
                  class="login-input"
                />
              </el-form-item>
              <el-form-item prop="username">
                <el-input
                  v-model="registerForm.username"
                  placeholder="请输入昵称（可选，默认同账号）"
                  size="large"
                  :prefix-icon="UserFilled"
                  class="login-input"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                  class="login-input"
                />
              </el-form-item>
              <el-form-item prop="email">
                <el-input
                  v-model="registerForm.email"
                  placeholder="邮箱（可选）"
                  size="large"
                  :prefix-icon="Message"
                  class="login-input"
                />
              </el-form-item>
              <el-button
                type="primary"
                size="large"
                class="w-full mt-2 login-btn"
                :loading="loading"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>

      <p class="text-center text-[#9499A0] text-xs mt-6">
        拾光旅记 - 记录你的每一个足迹
      </p>
    </div>

    <!-- Forgot Password Dialog -->
    <el-dialog v-model="showForgotDialog" title="重置密码" width="400px" :close-on-click-modal="false" class="rounded-2xl">
      <el-form :model="forgotForm" :rules="forgotRules" ref="forgotFormRef" label-width="80px">
        <el-form-item label="账号" prop="account">
          <el-input v-model="forgotForm.account" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="forgotForm.email" placeholder="请输入注册邮箱" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="forgotForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForgotDialog = false">取消</el-button>
        <el-button type="primary" :loading="forgotLoading" @click="handleForgotPassword">
          重置密码
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Lock, Message } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { forgotPassword } from '@/api/user'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

const loginForm = reactive({
  account: '',
  password: ''
})

const registerForm = reactive({
  account: '',
  username: '',
  password: '',
  email: ''
})

const showForgotDialog = ref(false)
const forgotLoading = ref(false)
const forgotFormRef = ref<FormInstance>()
const forgotForm = reactive({
  account: '',
  email: '',
  newPassword: ''
})

const loginRules: FormRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules: FormRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度3-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const forgotRules: FormRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.loginAction(loginForm)
        ElMessage.success('登录成功')
        router.push('/')
      } catch {
        // 错误已在 request 拦截器中处理，此处只需捕获避免冒泡
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.registerAction(registerForm)
        ElMessage.success('注册成功')
        router.push('/')
      } catch {
        // 错误已在 request 拦截器中处理
      } finally {
        loading.value = false
      }
    }
  })
}

const handleForgotPassword = async () => {
  if (!forgotFormRef.value) return
  await forgotFormRef.value.validate(async (valid) => {
    if (valid) {
      forgotLoading.value = true
      try {
        await forgotPassword(forgotForm)
        ElMessage.success('密码重置成功，请使用新密码登录')
        showForgotDialog.value = false
        forgotForm.account = ''
        forgotForm.email = ''
        forgotForm.newPassword = ''
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '重置失败')
      } finally {
        forgotLoading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-tabs :deep(.el-tabs__nav-wrap) {
  display: flex;
  justify-content: center;
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  padding: 0 32px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #E3E5E7;
  transition: all 0.2s ease;
}

.login-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #FFC1D3;
}

.login-btn {
  border-radius: 12px;
  font-weight: 600;
  font-size: 16px;
  height: 44px;
  background: linear-gradient(135deg, #FB7299, #E85D88);
  border: none;
  box-shadow: 0 4px 14px rgba(251, 114, 153, 0.35);
  transition: all 0.2s ease;
}

.login-btn:hover {
  box-shadow: 0 6px 20px rgba(251, 114, 153, 0.45);
  transform: translateY(-1px);
}
</style>

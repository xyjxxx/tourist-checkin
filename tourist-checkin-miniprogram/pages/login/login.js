const { accountLogin, register } = require('../../utils/auth')
const { request } = require('../../utils/api')

Page({
  data: {
    mode: 'login',  // login | register
    account: '',
    password: '',
    username: '',
    email: '',
    loading: false,
    loginSuccess: false
  },

  onSwitchMode() {
    this.setData({ mode: this.data.mode === 'login' ? 'register' : 'login' })
  },

  onForgotPassword() {
    wx.navigateTo({ url: '/pages/forgot-password/forgot-password' })
  },

  onAccountInput(e) {
    this.setData({ account: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onEmailInput(e) {
    this.setData({ email: e.detail.value })
  },

  onSubmit() {
    const { account, password, username, email, mode, loading } = this.data
    if (loading) return

    const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(account.trim())
    if (!account.trim()) {
      wx.showToast({ title: '请输入账号或邮箱', icon: 'none' })
      return
    }
    if (mode === 'register' && isEmail) {
      wx.showToast({ title: '注册请使用账号，不要用邮箱', icon: 'none' })
      return
    }
    if (mode === 'register' && (account.trim().length < 5 || account.trim().length > 13)) {
      wx.showToast({ title: '账号长度5-13位', icon: 'none' })
      return
    }
    if (mode === 'register' && !/^[a-zA-Z0-9]+$/.test(account.trim())) {
      wx.showToast({ title: '账号只能包含英文字母和数字', icon: 'none' })
      return
    }
    if (!password.trim() || password.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' })
      return
    }
    if (mode === 'register' && !username.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' })
      return
    }
    if (mode === 'register' && !email.trim()) {
      wx.showToast({ title: '请输入邮箱', icon: 'none' })
      return
    }
    if (mode === 'register' && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      wx.showToast({ title: '请输入正确的邮箱格式', icon: 'none' })
      return
    }

    this.setData({ loading: true })

    const action = mode === 'login'
      ? accountLogin(account.trim(), password)
      : register(account.trim(), password, username.trim(), email.trim())

    action
      .then(() => request({ url: '/user/info' }))
      .then(res => {
        const app = getApp()
        app.globalData.userInfo = res.data
        this.setData({ loginSuccess: true })
        setTimeout(() => {
          wx.switchTab({ url: '/pages/index/index' })
        }, 800)
      })
      .catch(err => {
        console.error('Login failed:', err)
        wx.showToast({ title: err.message || '登录失败，请重试', icon: 'none' })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  }
})

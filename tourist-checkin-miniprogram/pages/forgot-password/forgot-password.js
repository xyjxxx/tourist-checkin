const { request } = require('../../utils/api')

Page({
  data: {
    account: '',
    email: '',
    code: '',
    newPassword: '',
    loading: false,
    sendingCode: false,
    codeCooldown: 0
  },

  onAccountInput(e) { this.setData({ account: e.detail.value }) },
  onEmailInput(e) { this.setData({ email: e.detail.value }) },
  onCodeInput(e) { this.setData({ code: e.detail.value }) },
  onNewPasswordInput(e) { this.setData({ newPassword: e.detail.value }) },

  onUnload() {
    if (this._cooldownTimer) {
      clearInterval(this._cooldownTimer)
      this._cooldownTimer = null
    }
  },

  onSendCode() {
    const { email, sendingCode, codeCooldown } = this.data
    if (sendingCode || codeCooldown > 0) return
    if (!email.trim()) {
      wx.showToast({ title: '请输入邮箱地址', icon: 'none' })
      return
    }

    this.setData({ sendingCode: true })
    request({
      url: '/user/send-code',
      method: 'POST',
      data: { email: email.trim() }
    })
      .then(() => {
        wx.showToast({ title: '验证码已发送', icon: 'success' })
        this.setData({ codeCooldown: 60 })
        this._cooldownTimer = setInterval(() => {
          const next = this.data.codeCooldown - 1
          if (next <= 0) {
            clearInterval(this._cooldownTimer)
            this._cooldownTimer = null
            this.setData({ codeCooldown: 0 })
          } else {
            this.setData({ codeCooldown: next })
          }
        }, 1000)
      })
      .catch((err) => {
        wx.showToast({ title: err.message || '发送失败', icon: 'none' })
      })
      .finally(() => {
        this.setData({ sendingCode: false })
      })
  },

  onSubmit() {
    const { account, email, code, newPassword, loading } = this.data
    if (loading) return
    if (!account.trim()) { wx.showToast({ title: '请输入账号', icon: 'none' }); return }
    if (!email.trim()) { wx.showToast({ title: '请输入邮箱', icon: 'none' }); return }
    if (!code.trim()) { wx.showToast({ title: '请输入验证码', icon: 'none' }); return }
    if (!newPassword.trim() || newPassword.length < 6) { wx.showToast({ title: '密码至少6位', icon: 'none' }); return }

    this.setData({ loading: true })
    request({
      url: '/user/forgot-password',
      method: 'POST',
      data: {
        account: account.trim(),
        email: email.trim(),
        code: code.trim(),
        newPassword: newPassword
      }
    })
      .then(() => {
        wx.showToast({ title: '重置成功', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 1500)
      })
      .catch((err) => {
        wx.showToast({ title: err.message || '重置失败', icon: 'none' })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  onBack() { wx.navigateBack() }
})

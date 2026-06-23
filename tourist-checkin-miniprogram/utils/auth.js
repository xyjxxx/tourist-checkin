const { request } = require('./api')

function accountLogin(account, password) {
  return request({
    url: '/user/login',
    method: 'POST',
    data: { account, password }
  }).then(res => {
    wx.setStorageSync('token', res.data.token)
    return res.data
  })
}

function register(account, password, username, email) {
  return request({
    url: '/user/register',
    method: 'POST',
    data: { account, password, username, email }
  }).then(res => {
    wx.setStorageSync('token', res.data.token)
    return res.data
  })
}

function checkLogin() {
  return !!wx.getStorageSync('token')
}

function requireLogin() {
  if (!checkLogin()) {
    wx.showModal({
      title: '提示',
      content: '该功能需要登录，是否前往登录？',
      confirmText: '去登录',
      cancelText: '取消',
      success(res) {
        if (res.confirm) {
          wx.navigateTo({ url: '/pages/login/login' })
        }
      }
    })
    return false
  }
  return true
}

function logout() {
  wx.removeStorageSync('token')
  const app = getApp()
  if (app) app.globalData.userInfo = null
  wx.reLaunch({ url: '/pages/login/login' })
}

module.exports = { accountLogin, register, checkLogin, requireLogin, logout }

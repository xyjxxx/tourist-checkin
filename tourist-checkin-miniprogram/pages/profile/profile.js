const { request, uploadFile, fixImageUrl } = require('../../utils/api')
const { checkLogin, logout } = require('../../utils/auth')
const { animateCounter } = require('../../utils/animations')

const app = getApp()

Page({
  data: {
    userInfo: {},
    stats: {
      checkinCount: 0,
      followerCount: 0,
      followingCount: 0,
      points: 0
    },
    animatedStats: {
      checkinCount: 0,
      followerCount: 0,
      followingCount: 0,
      points: 0
    },
    unreadCount: 0
  },

  onLoad() {},

  onUnload() {
    if (this._animCleanups) {
      this._animCleanups.forEach(function (fn) { if (fn) fn() })
      this._animCleanups = null
    }
  },

  onShow() {
    if (this.getTabBar()) this.getTabBar().setSelected(4)
    if (checkLogin()) {
      this.loadUserInfo()
      this.loadUnreadCount()
    } else {
      this.setData({ userInfo: {}, stats: { checkinCount: 0, followerCount: 0, followingCount: 0, points: 0 } })
    }
  },

  onGoLogin() {
    wx.reLaunch({ url: '/pages/login/login' })
  },

  loadUserInfo() {
    // 清理旧动画定时器，防止泄漏
    if (this._animCleanups) {
      this._animCleanups.forEach((fn) => { if (typeof fn === 'function') fn() })
      this._animCleanups = null
    }
    request({ url: '/user/info', method: 'GET' })
      .then((res) => {
        const userInfo = res.data
        if (!userInfo) { wx.showToast({title:'用户信息不存在',icon:'none'}); return; }
        userInfo.avatar = fixImageUrl(userInfo.avatar)
        userInfo.backgroundImage = fixImageUrl(userInfo.backgroundImage)
        app.globalData.userInfo = userInfo
        this.setData({
          userInfo: userInfo,
          stats: {
            checkinCount: userInfo.checkinCount || 0,
            followerCount: userInfo.followerCount || 0,
            followingCount: userInfo.followingCount || 0,
            points: userInfo.points || 0
          }
        })
        this._animCleanups = [
          animateCounter(this, 'animatedStats.checkinCount', userInfo.checkinCount || 0, 800),
          animateCounter(this, 'animatedStats.followerCount', userInfo.followerCount || 0, 800),
          animateCounter(this, 'animatedStats.followingCount', userInfo.followingCount || 0, 800),
          animateCounter(this, 'animatedStats.points', userInfo.points || 0, 1000)
        ]
      })
      .catch((err) => {
        console.error('Load user info failed:', err)
      })
  },

  loadUnreadCount() {
    request({ url: '/notification/unread-count', method: 'GET' })
      .then((res) => {
        const count = res.data || 0
        this.setData({ unreadCount: count })
      })
      .catch((err) => { console.warn(err) })
  },

  // Edit avatar
  onEditAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const filePath = res.tempFiles[0].tempFilePath
        wx.showLoading({ title: '上传中...' })
        uploadFile(filePath)
          .then((uploadRes) => {
            const avatarUrl = uploadRes.data.url
            return request({
              url: '/user/avatar',
              method: 'PUT',
              data: { avatarUrl: avatarUrl }
            })
          })
          .then(() => {
            wx.showToast({ title: '头像更新成功', icon: 'success' })
            this.loadUserInfo()
          })
          .catch(() => {
            wx.showToast({ title: '更新失败', icon: 'none' })
          })
          .finally(() => {
            wx.hideLoading()
          })
      }
    })
  },

  // Edit nickname
  onEditUsername() {
    wx.showModal({
      title: '修改昵称',
      editable: true,
      placeholderText: '请输入新昵称',
      content: this.data.userInfo.username || '',
      success: (res) => {
        if (res.confirm && res.content && res.content.trim()) {
          const newUsername = res.content.trim()
          request({
            url: '/user/username',
            method: 'PUT',
            data: { username: newUsername }
          })
            .then(() => {
              wx.showToast({ title: '昵称更新成功', icon: 'success' })
              this.loadUserInfo()
            })
            .catch(() => {
              wx.showToast({ title: '更新失败', icon: 'none' })
            })
        }
      }
    })
  },

  // Edit background image
  onEditBackground() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const filePath = res.tempFiles[0].tempFilePath
        wx.showLoading({ title: '上传中...' })
        uploadFile(filePath)
          .then((uploadRes) => {
            const bgUrl = uploadRes.data.url
            return request({
              url: '/user/background',
              method: 'PUT',
              data: { imageUrl: bgUrl }
            })
          })
          .then(() => {
            wx.showToast({ title: '背景更新成功', icon: 'success' })
            this.loadUserInfo()
          })
          .catch(() => {
            wx.showToast({ title: '更新失败', icon: 'none' })
          })
          .finally(() => {
            wx.hideLoading()
          })
      }
    })
  },

  onTapStat(e) {
    const type = e.currentTarget.dataset.type
    if (type === 'followers' || type === 'following') {
      wx.navigateTo({ url: '/pages/follow-list/follow-list?tab=' + type })
    } else if (type === 'points') {
      wx.navigateTo({ url: '/pages/points/points' })
    } else if (type === 'checkins') {
      wx.navigateTo({ url: '/pages/checkin/my' })
    }
  },

  onTapMenu(e) {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const url = e.currentTarget.dataset.url
    wx.navigateTo({ url: url })
  },

  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          logout()
        }
      }
    })
  }
})

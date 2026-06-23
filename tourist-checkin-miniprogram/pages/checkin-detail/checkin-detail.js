const { request, fixImageUrls, fixImageUrl } = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')
const { formatTime } = require('../../utils/util')

Page({
  data: {
    checkin: null,
    loading: false,
    liked: false,
    isOwner: false,
    markers: [],
    comments: [],
    commentText: '',
    showCommentInput: false,
    replyingTo: null
  },

  onLoad(options) {
    if (options.id) {
      this.checkinId = options.id
      this.loadCheckin(options.id)
      this.loadComments(options.id)
    }
  },

  loadCheckin(id) {
    this.setData({ loading: true })
    request({ url: '/checkin/' + id, method: 'GET' })
      .then((res) => {
        const checkin = res.data
        if (!checkin) { wx.showToast({title:'内容不存在',icon:'none'}); setTimeout(()=>wx.navigateBack(),1500); return; }
        checkin.avatar = fixImageUrl(checkin.avatar)
        checkin.images = fixImageUrls(checkin.images)
        checkin.timeText = formatTime(checkin.checkInTime)
        const markers = []
        if (checkin.longitude && checkin.latitude) {
          markers.push({
            id: 1,
            longitude: checkin.longitude,
            latitude: checkin.latitude,
            width: 30,
            height: 30,
            callout: {
              content: checkin.locationName || '打卡位置',
              padding: 8,
              borderRadius: 4,
              display: 'ALWAYS'
            }
          })
        }
        const app = getApp()
        const currentUserId = app.globalData.userInfo && app.globalData.userInfo.id
        this.setData({ checkin, markers, liked: checkin.hasLiked || false, isOwner: currentUserId && checkin.userId === currentUserId })
      })
      .catch(() => {
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  loadComments(checkinId) {
    request({ url: '/comment/page', method: 'GET', data: { checkInId: checkinId, page: 1, size: 50 } })
      .then((res) => {
        const comments = (res.data || []).map(c => ({ ...c, timeText: formatTime(c.createdAt) }))
        this.setData({ comments })
      }).catch((err) => { console.warn(err) })
  },

  onLike() {
    if (this._liking) return
    if (!requireLogin()) return
    const checkin = this.data.checkin
    if (!checkin) return
    const prevLiked = this.data.liked
    const prevCount = checkin.likeCount || 0
    // Optimistic update
    this.setData({
      liked: !prevLiked,
      'checkin.likeCount': prevLiked ? Math.max(0, prevCount - 1) : prevCount + 1
    })
    this._liking = true
    request({ url: '/checkin/' + checkin.id + '/like', method: 'POST' }).catch(() => {
      // Revert on failure
      this.setData({ liked: prevLiked, 'checkin.likeCount': prevCount })
      wx.showToast({ title: '操作失败', icon: 'none' })
    }).finally(() => {
      this._liking = false
    })
  },

  onShowCommentInput() {
    if (!requireLogin()) return
    this.setData({ showCommentInput: true, replyingTo: null, commentText: '' })
  },

  onReplyComment(e) {
    if (!requireLogin()) return
    const { commentid, userid, username } = e.currentTarget.dataset
    this.setData({ showCommentInput: true, replyingTo: { commentId: commentid, userId: userid, username }, commentText: '' })
  },

  onCommentInput(e) {
    this.setData({ commentText: e.detail.value })
  },

  onSubmitComment() {
    const text = this.data.commentText.trim()
    if (!text) { wx.showToast({ title: '请输入评论内容', icon: 'none' }); return }
    const checkin = this.data.checkin
    if (!checkin) return
    const data = { checkInId: checkin.id, content: text }
    if (this.data.replyingTo) {
      data.parentId = this.data.replyingTo.commentId
      data.replyToId = this.data.replyingTo.commentId
      data.replyToUserId = this.data.replyingTo.userId
    }
    request({ url: '/comment', method: 'POST', data }).then(() => {
      wx.showToast({ title: '评论成功', icon: 'success' })
      this.setData({ commentText: '', showCommentInput: false, replyingTo: null })
      this.loadComments(checkin.id)
      const currentCount = (this.data.checkin && this.data.checkin.commentCount) || 0
      this.setData({ 'checkin.commentCount': currentCount + 1 })
    }).catch((err) => { console.warn(err) })
  },

  onCloseCommentInput() {
    this.setData({ showCommentInput: false, replyingTo: null })
  },

  onLikeComment(e) {
    if (!requireLogin()) return
    const commentId = e.currentTarget.dataset.id
    request({ url: '/comment/' + commentId + '/like', method: 'POST' }).then(() => {
      this.loadComments(this.data.checkin.id)
    }).catch((err) => { console.warn(err) })
  },

  onPreviewImage(e) {
    wx.previewImage({ current: e.currentTarget.dataset.src, urls: this.data.checkin.images || [] })
  },

  onReport() {
    if (!requireLogin()) return
    const reasons = ['虚假信息', '不当言论', '垃圾广告', '色情低俗', '其他原因']
    wx.showActionSheet({
      itemList: reasons,
      success: (res) => {
        const reason = reasons[res.tapIndex]
        wx.showModal({
          title: '举报确认',
          content: '确定举报此打卡吗？原因：' + reason,
          success: (modalRes) => {
            if (modalRes.confirm) {
              request({ url: '/report', method: 'POST', data: { targetType: 'CHECK_IN', targetId: this.data.checkin.id, reason } })
                .then(() => wx.showToast({ title: '举报已提交', icon: 'success' }))
                .catch(() => wx.showToast({ title: '举报失败', icon: 'none' }))
            }
          }
        })
      }
    })
  },

  onTapAuthor(e) {
    const userId = e.currentTarget.dataset.userid
    if (userId) wx.navigateTo({ url: '/pages/user-profile/user-profile?userId=' + userId })
  },

  onDelete() {
    wx.showModal({
      title: '删除打卡',
      content: '确定要删除这条打卡记录吗？删除后无法恢复。',
      confirmColor: '#FF3B30',
      success: (res) => {
        if (res.confirm) {
          request({ url: '/checkin/' + this.checkinId, method: 'DELETE' })
            .then(() => {
              wx.showToast({ title: '删除成功', icon: 'success' })
              getApp().globalData.needRefreshFeed = true
              setTimeout(() => { wx.navigateBack() }, 1000)
            })
            .catch(() => {
              wx.showToast({ title: '删除失败', icon: 'none' })
            })
        }
      }
    })
  },

  onShareAppMessage() {
    const checkin = this.data.checkin
    return {
      title: checkin ? (checkin.content || '来看看我的打卡') : '拾光旅记',
      path: '/pages/checkin-detail/checkin-detail?id=' + this.checkinId
    }
  }
})

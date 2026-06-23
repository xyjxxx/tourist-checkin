const { request, fixImageUrls, fixImageUrl } = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')
const { formatTime } = require('../../utils/util')

Page({
  data: {
    note: null,
    loading: false,
    liked: false,
    collected: false,
    comments: [],
    commentText: '',
    showCommentInput: false,
    replyingTo: null
  },

  onLoad(options) {
    if (options.id) {
      this.noteId = options.id
      this.loadNote(options.id)
      this.loadComments(options.id)
    }
  },

  loadNote(id) {
    this.setData({ loading: true })
    request({ url: '/travel-note/' + id, method: 'GET' })
      .then((res) => {
        const note = res.data
        if (!note) { wx.showToast({title:'游记不存在',icon:'none'}); setTimeout(()=>wx.navigateBack(),1500); return; }
        note.avatar = fixImageUrl(note.avatar)
        note.images = fixImageUrls(note.images)
        if (note.author) note.author.avatar = fixImageUrl(note.author.avatar)
        note.coverImage = fixImageUrl(note.coverImage)
        note.timeText = formatTime(note.createdAt)
        this.setData({
          note: note,
          liked: note.hasLiked || false,
          collected: note.hasCollected || false
        })
      })
      .catch(() => {
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  loadComments(noteId) {
    request({ url: '/comment/page', method: 'GET', data: { noteId: noteId, page: 1, size: 50 } })
      .then((res) => {
        const comments = (res.data || []).map(c => ({
          ...c,
          timeText: formatTime(c.createdAt)
        }))
        this.setData({ comments: comments })
      }).catch((err) => { console.warn(err) })
  },

  onLike() {
    if (this._liking) return
    if (!requireLogin()) return
    const note = this.data.note
    if (!note) return
    const prevLiked = this.data.liked
    const prevCount = note.likeCount || 0
    this.setData({
      liked: !prevLiked,
      'note.likeCount': prevLiked ? Math.max(0, prevCount - 1) : prevCount + 1
    })
    this._liking = true
    request({ url: '/travel-note/' + note.id + '/like', method: 'POST' }).catch(() => {
      this.setData({ liked: prevLiked, 'note.likeCount': prevCount })
      wx.showToast({ title: '操作失败', icon: 'none' })
    }).finally(() => {
      this._liking = false
    })
  },

  onCollect() {
    if (this._collecting) return
    if (!requireLogin()) return
    const note = this.data.note
    if (!note) return
    const prev = this.data.collected
    const nowCollected = !prev
    this.setData({ collected: nowCollected })
    this._collecting = true
    request({ url: '/travel-note/' + note.id + '/collect', method: 'POST' }).then(() => {
      wx.showToast({ title: nowCollected ? '已收藏' : '已取消收藏', icon: 'none' })
    }).catch(() => {
      this.setData({ collected: prev })
      wx.showToast({ title: '操作失败', icon: 'none' })
    }).finally(() => {
      this._collecting = false
    })
  },

  // Report functionality
  onReport() {
    if (!requireLogin()) return
    const reportReasons = [
      '虚假信息',
      '抄袭内容',
      '不当言论',
      '垃圾广告',
      '色情低俗',
      '其他原因'
    ]
    wx.showActionSheet({
      itemList: reportReasons,
      success: (res) => {
        const reason = reportReasons[res.tapIndex]
        wx.showModal({
          title: '举报确认',
          content: '确定要举报此游记吗？原因：' + reason,
          success: (modalRes) => {
            if (modalRes.confirm) {
              request({
                url: '/report',
                method: 'POST',
                data: {
                  targetType: 'TRAVEL_NOTE',
                  targetId: this.data.note.id,
                  reason: reason
                }
              })
                .then(() => {
                  wx.showToast({ title: '举报已提交', icon: 'success' })
                })
                .catch(() => {
                  wx.showToast({ title: '举报失败', icon: 'none' })
                })
            }
          }
        })
      }
    })
  },

  // 评论功能
  onShowCommentInput() {
    if (!requireLogin()) return
    this.setData({ showCommentInput: true, replyingTo: null, commentText: '' })
  },

  onReplyComment(e) {
    if (!requireLogin()) return
    const { commentid, userid, username } = e.currentTarget.dataset
    this.setData({
      showCommentInput: true,
      replyingTo: { commentId: commentid, userId: userid, username: username },
      commentText: ''
    })
  },

  onCommentInput(e) {
    this.setData({ commentText: e.detail.value })
  },

  onSubmitComment() {
    const text = this.data.commentText.trim()
    if (!text) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' })
      return
    }
    const note = this.data.note
    if (!note) return

    const data = { noteId: note.id, content: text }
    if (this.data.replyingTo) {
      data.parentId = this.data.replyingTo.commentId
      data.replyToId = this.data.replyingTo.commentId
      data.replyToUserId = this.data.replyingTo.userId
    }

    request({ url: '/comment', method: 'POST', data: data }).then(() => {
      wx.showToast({ title: '评论成功', icon: 'success' })
      this.setData({ commentText: '', showCommentInput: false, replyingTo: null })
      this.loadComments(note.id)
      const currentCount = (this.data.note && this.data.note.commentCount) || 0
      this.setData({ 'note.commentCount': currentCount + 1 })
    }).catch((err) => { console.warn(err) })
  },

  onCloseCommentInput() {
    this.setData({ showCommentInput: false, replyingTo: null })
  },

  onLikeComment(e) {
    if (!requireLogin()) return
    const commentId = e.currentTarget.dataset.id
    request({ url: '/comment/' + commentId + '/like', method: 'POST' }).then(() => {
      this.loadComments(this.data.note.id)
    }).catch((err) => { console.warn(err) })
  },

  onPreviewImage(e) {
    const src = e.currentTarget.dataset.src
    wx.previewImage({ current: src, urls: this.data.note.images || [] })
  },

  onTapAuthor(e) {
    const userId = e.currentTarget.dataset.userid
    if (userId) {
      wx.navigateTo({ url: '/pages/user-profile/user-profile?userId=' + userId })
    }
  },

  onShareAppMessage() {
    const note = this.data.note
    return {
      title: note ? note.title : '拾光旅记',
      path: '/pages/note-detail/note-detail?id=' + this.noteId
    }
  }
})

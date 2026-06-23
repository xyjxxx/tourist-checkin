Component({
  properties: {
    comment: {
      type: Object,
      value: {}
    }
  },

  methods: {
    onLike() {
      this.triggerEvent('like', { comment: this.data.comment })
    },

    onReply() {
      this.triggerEvent('reply', { comment: this.data.comment })
    },

    onTapUsername() {
      var userId = this.data.comment.userId
      if (userId) {
        wx.navigateTo({ url: '/pages/user-profile/user-profile?userId=' + userId })
      }
    }
  }
})

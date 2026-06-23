Component({
  properties: {
    checkin: {
      type: Object,
      value: {}
    }
  },

  methods: {
    onLike(e) {
      const id = e.currentTarget.dataset.id
      this.triggerEvent('like', { id: id })
    },

    onTap() {
      this.triggerEvent('tap', { checkin: this.data.checkin })
    },

    onPreviewImage(e) {
      const src = e.currentTarget.dataset.src
      wx.previewImage({
        current: src,
        urls: this.data.checkin.images || []
      })
    }
  }
})

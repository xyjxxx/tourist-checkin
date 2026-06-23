Component({
  properties: {
    user: {
      type: Object,
      value: {}
    },
    isFollowing: {
      type: Boolean,
      value: false
    }
  },

  methods: {
    onToggleFollow() {
      this.triggerEvent('toggleFollow', {
        userId: this.data.user.id,
        isFollowing: this.data.isFollowing
      })
    }
  }
})

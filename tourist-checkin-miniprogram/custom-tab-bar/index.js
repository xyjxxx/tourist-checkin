Component({
  data: {
    selected: 0
  },

  methods: {
    setSelected: function (index) {
      this.setData({ selected: index })
    },

    switchTab: function (e) {
      var index = e.currentTarget.dataset.index
      var url = e.currentTarget.dataset.url
      this.setData({ selected: index })
      wx.switchTab({ url: url })
    }
  }
})

Component({
  properties: {
    type: { type: String, value: 'checkin' },
    count: { type: Number, value: 3 }
  },

  data: {
    items: []
  },

  lifetimes: {
    attached: function () {
      this._buildItems()
    }
  },

  observers: {
    'count': function () {
      this._buildItems()
    }
  },

  methods: {
    _buildItems: function () {
      var items = []
      for (var i = 0; i < this.data.count; i++) {
        items.push(i)
      }
      this.setData({ items: items })
    }
  }
})

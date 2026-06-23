Component({
  properties: {
    actions: {
      type: Array,
      value: []
    },
    safeArea: {
      type: Boolean,
      value: true
    }
  },

  methods: {
    onAction: function (e) {
      var type = e.currentTarget.dataset.type
      this.triggerEvent('action', { type: type })
    }
  }
})

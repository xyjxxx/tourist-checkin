Component({
  properties: {
    icon: { type: String, value: '' },
    title: { type: String, value: '' },
    subtitle: { type: String, value: '' },
    lottiePath: { type: String, value: '' },
    actionText: { type: String, value: '' }
  },

  methods: {
    onAction: function () {
      this.triggerEvent('action')
    }
  }
})

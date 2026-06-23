const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')

Page({
  data: {
    achievements: [],
    loading: false,
    showUnlockAnimation: false,
    unlockedAchievement: null
  },

  onLoad() {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadAchievements()
  },

  loadAchievements() {
    this.setData({ loading: true })

    Promise.all([
      request({ url: '/achievement/my', method: 'GET' }),
      request({ url: '/achievement/definitions', method: 'GET' })
    ])
      .then(([myRes, defRes]) => {
        const myAchievements = myRes.data || []
        const definitions = defRes.data || []
        const myMap = {}
        myAchievements.forEach((item) => {
          myMap[item.achievementId] = item
        })

        const achievements = definitions.map((def) => {
          const mine = myMap[def.id]
          return {
            id: def.id,
            name: def.name,
            description: def.description,
            icon: def.icon,
            unlocked: !!mine,
            progress: mine ? (mine.progress || 100) : 0
          }
        })

        this.setData({ achievements: achievements })
      })
      .catch((err) => {
        console.error('Load achievements failed:', err)
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  showUnlockEffect(achievement) {
    this.setData({
      showUnlockAnimation: true,
      unlockedAchievement: achievement
    })
    setTimeout(() => {
      this.setData({ showUnlockAnimation: false })
    }, 2500)
  },

  onCloseUnlock() {
    this.setData({ showUnlockAnimation: false })
  }
})

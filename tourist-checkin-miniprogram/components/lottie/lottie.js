/**
 * Lottie 动画组件
 * 优先使用 lottie-miniprogram 渲染真实 JSON 动画
 * 若库未安装或加载失败，降级为 CSS 动画
 *
 * 属性:
 *   path      {string}  动画文件路径或类型标识
 *   autoplay  {boolean} 是否自动播放，默认 true
 *   loop      {boolean} 是否循环，默认 false
 *   width     {number}  宽度(rpx)，默认 200
 *   height    {number}  高度(rpx)，默认 200
 */

var instanceId = 0
var lottieLib = null
var lottieLoaded = false

function getLottieLib() {
  if (lottieLoaded) return lottieLib
  lottieLoaded = true
  try {
    lottieLib = require('lottie-miniprogram')
  } catch (e) {
    lottieLib = null
  }
  return lottieLib
}

Component({
  properties: {
    path: { type: String, value: '' },
    autoplay: { type: Boolean, value: true },
    loop: { type: Boolean, value: false },
    width: { type: Number, value: 200 },
    height: { type: Number, value: 200 }
  },

  data: {
    animType: 'none',
    useFallback: true,
    canvasId: ''
  },

  lifetimes: {
    attached: function () {
      this.setData({ canvasId: 'lottie-' + (++instanceId) })
    },

    ready: function () {
      if (this.data.path) {
        this._loadAnimation(this.data.path)
      }
    },

    detached: function () {
      this._destroyLottie()
    }
  },

  observers: {
    'path': function (p) {
      if (!p) return
      this._loadAnimation(p)
    }
  },

  methods: {
    _loadAnimation: function (path) {
      this._destroyLottie()

      var isJsonPath = path.indexOf('.json') !== -1 || path.indexOf('/assets/lottie/') !== -1
      var lib = getLottieLib()

      if (lib && isJsonPath) {
        this._loadRealLottie(path, lib)
      } else {
        this._setFallbackType(path)
      }
    },

    _loadRealLottie: function (path, lib) {
      var self = this
      var query = wx.createSelectorQuery().in(this)
      query.select('#' + this.data.canvasId)
        .fields({ node: true, size: true })
        .exec(function (res) {
          if (!res || !res[0] || !res[0].node) {
            self._setFallbackType(self.data.path)
            return
          }

          var canvas = res[0].node
          var ctx = canvas.getContext('2d')
          var dpr = wx.getWindowInfo().pixelRatio
          canvas.width = res[0].width * dpr
          canvas.height = res[0].height * dpr
          ctx.scale(dpr, dpr)

          try {
            self._lottieInstance = lib.loadAnimation({
              renderer: 'canvas',
              loop: self.data.loop,
              autoplay: self.data.autoplay,
              path: path,
              rendererSettings: {
                context: ctx,
                canvas: canvas
              }
            })

            self._lottieInstance.addEventListener('complete', function () {
              self.triggerEvent('complete')
            })

            self.setData({ useFallback: false })
          } catch (e) {
            self._setFallbackType(self.data.path)
          }
        })
    },

    _setFallbackType: function (p) {
      if (!p) {
        this.setData({ animType: 'none', useFallback: true })
        return
      }

      var type = 'none'
      if (p.indexOf('achievement') !== -1 || p.indexOf('unlock') !== -1) {
        type = 'unlock'
      } else if (p.indexOf('success') !== -1 || p.indexOf('login') !== -1 || p.indexOf('check') !== -1) {
        type = 'success'
      } else if (p.indexOf('empty') !== -1) {
        type = 'empty'
      } else if (p.indexOf('confetti') !== -1) {
        type = 'confetti'
      }

      this.setData({ animType: type, useFallback: true })
    },

    _destroyLottie: function () {
      if (this._lottieInstance) {
        try {
          this._lottieInstance.destroy()
        } catch (e) {}
        this._lottieInstance = null
      }
    },

    play: function () {
      if (this._lottieInstance) this._lottieInstance.play()
    },

    pause: function () {
      if (this._lottieInstance) this._lottieInstance.pause()
    },

    stop: function () {
      if (this._lottieInstance) this._lottieInstance.stop()
    }
  }
})

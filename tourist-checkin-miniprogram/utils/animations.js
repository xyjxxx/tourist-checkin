/**
 * 动画工具模块
 * animateCounter   - 数字递增动画
 * animateValue     - 通用数值动画（支持自定义起止值和格式化）
 * staggerReveal    - 列表项逐个渐显
 * initScrollReveal - IntersectionObserver 滚动揭示（增强版）
 * createSpring     - 弹簧动画辅助
 */

/**
 * 数字从 0 递增到目标值（带 ease-out 缓动）
 * @param {Page} page - Page 实例 (this)
 * @param {string} dataKey - setData 的 key
 * @param {number} target - 目标数值
 * @param {number} duration - 动画时长(ms)，默认 800
 */
function animateCounter(page, dataKey, target, duration) {
  return animateValue(page, dataKey, 0, target, duration)
}

/**
 * 通用数值动画
 * @param {Page} page - Page 实例
 * @param {string} dataKey - setData 的 key
 * @param {number} from - 起始值
 * @param {number} to - 目标值
 * @param {number} duration - 动画时长(ms)，默认 800
 * @param {Object} options - 可选配置
 * @param {Function} options.format - 格式化函数，接收当前数值，返回显示值
 * @param {Function} options.onComplete - 动画完成回调
 */
function animateValue(page, dataKey, from, to, duration, options) {
  options = options || {}
  duration = duration || 800

  if (!page) return

  if (from === to) {
    page.setData({ [dataKey]: options.format ? options.format(to) : to })
    return
  }

  var startTime = Date.now()
  var diff = to - from
  var cancelled = false
  var timerId = null

  function step() {
    if (cancelled) return
    var elapsed = Date.now() - startTime
    var progress = Math.min(elapsed / duration, 1)
    var eased = 1 - Math.pow(1 - progress, 3)
    var current = Math.round(from + diff * eased)
    var displayValue = options.format ? options.format(current) : current
    page.setData({ [dataKey]: displayValue })

    if (progress < 1) {
      timerId = setTimeout(step, 16)
    } else if (options.onComplete) {
      options.onComplete()
    }
  }

  step()

  return function () {
    cancelled = true
    if (timerId) clearTimeout(timerId)
  }
}

/**
 * 列表项逐个渐显（stagger reveal）
 * @param {Page} page - Page 实例
 * @param {string} listKey - 列表数据的 key（如 'list'）
 * @param {Object} options
 * @param {number} options.delay - 每项间隔(ms)，默认 40
 * @param {number} options.startDelay - 首项延迟(ms)，默认 0
 * @param {string} options.revealKey - 控制显示的字段名，默认 'visible'
 */
function staggerReveal(page, listKey, options) {
  options = options || {}
  var delay = options.delay || 40
  var startDelay = options.startDelay || 0
  var revealKey = options.revealKey || 'visible'

  if (!page) return

  var list = page.data[listKey]
  if (!list || !list.length) return

  var timers = []

  list.forEach(function (item, idx) {
    var timer = setTimeout(function () {
      page.setData({
        [listKey + '[' + idx + '].' + revealKey]: true
      })
    }, startDelay + idx * delay)
    timers.push(timer)
  })

  return function () {
    timers.forEach(clearTimeout)
  }
}

/**
 * 初始化滚动揭示动画（增强版）
 * 支持多阈值、自定义类名、自动 stagger
 *
 * 使用方式：
 * 1. WXML 中给元素添加 class="anim-reveal {{revealed[idx] ? 'is-visible' : ''}}"
 *    并添加 data-reveal-idx="{{idx}}"
 * 2. 在 onLoad 中调用 initScrollReveal(this, { threshold: 0.15 })
 * 3. 在 onUnload 中调用 cleanup() 清理 observer
 *
 * @param {Page} page - Page 实例
 * @param {Object} options
 * @param {number} options.threshold - 交叉比例阈值，默认 0.15
 * @param {number} options.bottom - 底部偏移(px)，默认 -50
 * @param {string} options.selector - 观察的选择器，默认 '.anim-reveal'
 * @param {boolean} options.autoStagger - 自动添加 stagger 延迟，默认 false
 * @param {number} options.staggerDelay - stagger 间隔(ms)，默认 50
 * @returns {Function} cleanup 清理函数
 */
function initScrollReveal(page, options) {
  options = options || {}
  var threshold = options.threshold || 0.15
  var bottom = options.bottom || -50
  var selector = options.selector || '.anim-reveal'
  var autoStagger = options.autoStagger || false
  var staggerDelay = options.staggerDelay || 50

  if (!page || !page.createIntersectionObserver) return function () {}

  var revealOrder = 0
  var observer = page.createIntersectionObserver({
    thresholds: [threshold],
    observeAll: true
  })

  observer.relativeToViewport({ bottom: bottom })

  observer.observe(selector, function (res) {
    if (res.intersectionRatio >= threshold) {
      var idx = res.dataset && res.dataset.revealIdx
      if (idx !== undefined && idx !== null) {
        if (autoStagger) {
          var delay = revealOrder * staggerDelay
          revealOrder++
          setTimeout(function () {
            page.setData({ ['revealed.' + idx]: true })
          }, delay)
        } else {
          page.setData({ ['revealed.' + idx]: true })
        }
      }
    }
  })

  return function () {
    observer.disconnect()
    revealOrder = 0
  }
}

/**
 * 弹簧动画辅助
 * 使用 wx.createAnimation 生成弹簧效果
 * @param {Object} properties - 动画属性 { scale, translateX, translateY, rotate, opacity }
 * @param {number} duration - 动画时长(ms)，默认 300
 * @returns {Object} wx.Animation 实例
 */
function createSpring(properties, duration) {
  properties = properties || {}
  duration = duration || 300

  var animation = wx.createAnimation({
    duration: duration,
    timingFunction: 'ease-out'
  })

  if (properties.scale !== undefined) {
    animation.scale(properties.scale)
  }
  if (properties.translateX !== undefined) {
    animation.translateX(properties.translateX)
  }
  if (properties.translateY !== undefined) {
    animation.translateY(properties.translateY)
  }
  if (properties.rotate !== undefined) {
    animation.rotate(properties.rotate)
  }
  if (properties.opacity !== undefined) {
    animation.opacity(properties.opacity)
  }

  return animation.export()
}

module.exports = {
  animateCounter: animateCounter,
  animateValue: animateValue,
  staggerReveal: staggerReveal,
  initScrollReveal: initScrollReveal,
  createSpring: createSpring
}

import { ref, watch } from 'vue'

const isDark = ref(false)

// 初始化：从 localStorage 读取
const saved = localStorage.getItem('darkMode')
if (saved !== null) {
  isDark.value = saved === 'true'
} else {
  // 跟随系统偏好
  isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
}

watch(isDark, (val) => {
  localStorage.setItem('darkMode', String(val))
})

// 跨标签页同步暗黑模式
window.addEventListener('storage', (e) => {
  if (e.key === 'darkMode' && e.newValue !== null) {
    isDark.value = e.newValue === 'true'
  }
})

export function useDarkMode() {
  const toggle = () => {
    isDark.value = !isDark.value
  }

  return { isDark, toggle }
}

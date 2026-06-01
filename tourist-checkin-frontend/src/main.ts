import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import router from './router'
import App from './App.vue'
import './assets/styles/main.css'

// 配置 dayjs
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const app = createApp(App)

// 只注册常用图标（按需注册）
import {
  HomeFilled, Search, Plus, ArrowRight, View, Star, StarFilled,
  Delete, Setting, Medal, Coin, DataAnalysis, User, Lock, Message,
  FolderChecked, FolderAdd, Edit, Picture, Location, Clock,
  ChatDotRound, Bell, Close, Check, ArrowLeft, ArrowDown,
  Upload, Refresh, Filter, Menu, More
} from '@element-plus/icons-vue'

const icons = {
  HomeFilled, Search, Plus, ArrowRight, View, Star, StarFilled,
  Delete, Setting, Medal, Coin, DataAnalysis, User, Lock, Message,
  FolderChecked, FolderAdd, Edit, Picture, Location, Clock,
  ChatDotRound, Bell, Close, Check, ArrowLeft, ArrowDown,
  Upload, Refresh, Filter, Menu, More
}

for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn, zIndex: 3000 })

app.mount('#app')

<template>
  <div class="relative w-full h-full">
    <div id="map" ref="mapContainer" class="w-full h-full"></div>

    <!-- 定位按钮 -->
    <button
      class="absolute bottom-6 right-6 z-10 w-12 h-12 rounded-full bg-white shadow-lg flex items-center justify-center hover:shadow-xl transition-all duration-200 hover:scale-105 border border-gray-100"
      @click="locateMe"
      :class="{ 'animate-pulse': locating }"
    >
      <svg class="w-6 h-6" :class="currentLng ? 'text-[#FB7299]' : 'text-gray-400'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="3"/>
        <path d="M12 2v4M12 18v4M2 12h4M18 12h4"/>
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { ElMessage } from 'element-plus'
import type { CheckIn } from '@/types'

interface Props {
  checkIns: CheckIn[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  markerClick: [checkIn: CheckIn]
  mapClick: [lng: number, lat: number]
}>()

const mapContainer = ref<HTMLDivElement>()
let map: any = null
let AMapInstance: any = null
let markers: any[] = []
let currentLocationMarker: any = null
const locating = ref(false)
const currentLng = ref<number | null>(null)
const currentLat = ref<number | null>(null)

const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || ''

const initMap = async () => {
  if (!AMAP_KEY) {
    ElMessage.warning('请配置高德地图 Key')
    return
  }

  try {
    AMapInstance = await AMapLoader.load({
      key: AMAP_KEY,
      version: '2.0',
      securityConfig: {
        securityJsCode: import.meta.env.VITE_AMAP_SECURITY_CODE || ''
      },
      plugins: ['AMap.Marker', 'AMap.InfoWindow', 'AMap.Geocoder']
    })

    let centerLng = 116.397428
    let centerLat = 39.90923
    try {
      const position = await new Promise<GeolocationPosition>((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 5000 })
      })
      centerLng = position.coords.longitude
      centerLat = position.coords.latitude
      currentLng.value = centerLng
      currentLat.value = centerLat
    } catch {
      // fallback to default
    }

    map = new AMapInstance.Map('map', {
      zoom: 11,
      center: [centerLng, centerLat],
      mapStyle: 'amap://styles/light',
    })

    map.on('click', (e: any) => {
      emit('mapClick', e.lnglat.lng, e.lnglat.lat)
    })

    updateMarkers()

    // 如果已获取到位置，显示当前位置标记
    if (currentLng.value && currentLat.value) {
      showCurrentLocationMarker(currentLng.value, currentLat.value)
    }
  } catch (error) {
    console.error('地图加载失败:', error)
    ElMessage.error('地图加载失败，请检查网络连接')
  }
}

const escapeHtml = (str: string): string => {
  const div = document.createElement('div')
  div.appendChild(document.createTextNode(str))
  return div.innerHTML
}

const createMarkerContent = (name: string, isActive: boolean) => {
  const color = isActive ? '#FB7299' : '#FF85AB'
  const size = isActive ? 44 : 34
  const safeName = escapeHtml(name)

  return `<div style="
    display:flex;flex-direction:column;align-items:center;
    transform: translate(-50%, -100%);
    cursor: pointer;
  ">
    <div style="
      width:${size}px;height:${size + 10}px;
      background:${color};
      border-radius: ${size}px ${size}px ${size}px 4px;
      transform: rotate(-45deg);
      box-shadow: 0 4px 12px ${color}44;
      border: 3px solid white;
    "></div>
    <div style="
      margin-top:6px;padding:2px 8px;
      background:white;border-radius:6px;
      font-size:11px;font-weight:600;color:#18191C;
      white-space:nowrap;
      box-shadow: 0 1px 4px rgba(0,0,0,0.08);
    ">${safeName}</div>
  </div>`
}

// 当前位置标记 - 蓝色脉冲圆点
const createCurrentLocationContent = () => {
  return `<div style="position:relative;width:40px;height:40px;transform:translate(-50%,-50%)">
    <div style="
      position:absolute;top:0;left:0;width:40px;height:40px;
      border-radius:50%;
      background:rgba(59,130,246,0.15);
      animation:locPulse 2s ease-out infinite;
    "></div>
    <div style="
      position:absolute;top:10px;left:10px;width:20px;height:20px;
      border-radius:50%;
      background:#3B82F6;
      border:3px solid white;
      box-shadow:0 0 12px rgba(59,130,246,0.5);
    "></div>
  </div>
  <style>
    @keyframes locPulse {
      0% { transform:scale(1); opacity:0.6; }
      100% { transform:scale(2.5); opacity:0; }
    }
  </style>`
}

const showCurrentLocationMarker = (lng: number, lat: number) => {
  if (!map || !AMapInstance) return

  // 移除旧的当前位置标记
  if (currentLocationMarker) {
    map.remove(currentLocationMarker)
    currentLocationMarker = null
  }

  currentLocationMarker = new AMapInstance.Marker({
    position: [lng, lat],
    content: createCurrentLocationContent(),
    zIndex: 200,
  })

  map.add(currentLocationMarker)
}

const locateMe = () => {
  if (!navigator.geolocation) {
    ElMessage.error('浏览器不支持地理定位')
    return
  }

  locating.value = true
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const lng = position.coords.longitude
      const lat = position.coords.latitude
      currentLng.value = lng
      currentLat.value = lat

      // 移动地图中心到当前位置
      map.setCenter([lng, lat])
      map.setZoom(14)

      // 显示当前位置标记
      showCurrentLocationMarker(lng, lat)

      locating.value = false
      ElMessage.success('已定位到当前位置')
    },
    (error) => {
      locating.value = false
      switch (error.code) {
        case error.PERMISSION_DENIED:
          ElMessage.error('定位权限被拒绝，请在浏览器设置中允许定位')
          break
        case error.POSITION_UNAVAILABLE:
          ElMessage.error('位置信息不可用')
          break
        case error.TIMEOUT:
          ElMessage.error('定位超时，请重试')
          break
        default:
          ElMessage.error('定位失败')
      }
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  )
}

const updateMarkers = () => {
  if (!map || !AMapInstance) return

  markers.forEach(marker => map.remove(marker))
  markers = []

  const coordMap = new Map<string, number>()
  props.checkIns.forEach(c => {
    const key = `${c.longitude},${c.latitude}`
    coordMap.set(key, (coordMap.get(key) || 0) + 1)
  })

  const coordIndex = new Map<string, number>()

  props.checkIns.forEach(checkIn => {
    const key = `${checkIn.longitude},${checkIn.latitude}`
    const count = coordMap.get(key) || 1
    const idx = coordIndex.get(key) || 0
    coordIndex.set(key, idx + 1)

    let offsetLng = 0
    let offsetLat = 0
    if (count > 1 && idx > 0) {
      const angle = (idx / count) * Math.PI * 2
      const radius = 0.0003 * count
      offsetLng = Math.cos(angle) * radius
      offsetLat = Math.sin(angle) * radius
    }

    const marker = new AMapInstance.Marker({
      position: [checkIn.longitude + offsetLng, checkIn.latitude + offsetLat],
      title: checkIn.locationName,
      content: createMarkerContent(checkIn.locationName, false),
      offset: new AMapInstance.Pixel(0, 0),
      zIndex: 100,
    })

    marker.on('click', () => {
      emit('markerClick', checkIn)
    })

    map.add(marker)
    markers.push(marker)
  })
}

const centerToLocation = (lng: number, lat: number) => {
  if (map) {
    map.setCenter([lng, lat])
    map.setZoom(14)
  }
}

watch(() => props.checkIns.length + props.checkIns.map(c => `${c.longitude},${c.latitude}`).join(','), updateMarkers)

onMounted(initMap)

onUnmounted(() => {
  if (map) {
    map.destroy()
  }
})

defineExpose({
  centerToLocation,
  locateMe
})
</script>

<style scoped>
#map {
  width: 100%;
  height: 100%;
}
</style>

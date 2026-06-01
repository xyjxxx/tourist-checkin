<template>
  <el-dialog
    v-model="visible"
    width="580px"
    :close-on-click-modal="false"
    class="checkin-dialog"
  >
    <template #header>
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-[#FF85AB] to-[#FB7299] flex items-center justify-center">
          <el-icon color="white" :size="18"><MapLocation /></el-icon>
        </div>
        <div>
          <h3 class="text-lg font-bold text-[#18191C]">记录这一刻</h3>
          <p class="text-xs text-[#9499A0]">分享你去过的地方</p>
        </div>
      </div>
    </template>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" class="mt-2">
      <el-form-item label="地点名称" prop="locationName">
        <el-input v-model="form.locationName" placeholder="请输入地点名称" size="large">
          <template #append>
            <el-button :icon="MapLocation" class="locate-btn" @click="getCurrentLocation">
              定位
            </el-button>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="坐标">
        <div class="flex gap-3">
          <el-input v-model="form.longitude" placeholder="经度" readonly size="large">
            <template #prepend>经度</template>
          </el-input>
          <el-input v-model="form.latitude" placeholder="纬度" readonly size="large">
            <template #prepend>纬度</template>
          </el-input>
        </div>
      </el-form-item>

      <el-form-item label="打卡内容">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="3"
          placeholder="记录此刻的心情..."
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="上传照片">
        <el-upload
          v-model:file-list="fileList"
          action="#"
          list-type="picture-card"
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="9"
          accept="image/*"
        >
          <div class="upload-placeholder">
            <el-icon :size="24" color="#FFC1D3"><Plus /></el-icon>
            <span class="text-xs text-[#9499A0] mt-1">添加照片</span>
          </div>
        </el-upload>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="flex justify-end gap-3">
        <el-button size="large" @click="visible = false">取消</el-button>
        <el-button size="large" type="primary" :loading="loading" @click="submitCheckIn" class="px-8">
          确定打卡
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, MapLocation } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { useCheckInStore } from '@/stores/checkin'
import { uploadFiles } from '@/api/file'
import { compressImage } from '@/utils/image-compress'
import AMapLoader from '@amap/amap-jsapi-loader'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const checkInStore = useCheckInStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const fileList = ref<UploadFile[]>([])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive({
  locationName: '',
  longitude: '',
  latitude: '',
  content: ''
})

const rules: FormRules = {
  locationName: [{ required: true, message: '请输入地点名称', trigger: 'blur' }],
  longitude: [{ required: true, message: '请获取坐标', trigger: 'blur' }],
  latitude: [{ required: true, message: '请获取坐标', trigger: 'blur' }]
}

const getCurrentLocation = async () => {
  if (!navigator.geolocation) {
    ElMessage.error('浏览器不支持地理定位')
    return
  }

  const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || ''
  if (!AMAP_KEY) {
    ElMessage.error('未配置高德地图 Key')
    return
  }

  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const lat = position.coords.latitude
      const lng = position.coords.longitude
      form.latitude = lat.toFixed(6)
      form.longitude = lng.toFixed(6)

      try {
        const AMap = await AMapLoader.load({
          key: AMAP_KEY,
          version: '2.0'
        })

        AMap.plugin('AMap.Geocoder', () => {
          const geocoder = new AMap.Geocoder()
          geocoder.getAddress([lng, lat], (status: string, result: any) => {
            if (status === 'complete' && result.regeocode) {
              const regeocode = result.regeocode
              const poi = regeocode.pois?.[0]
              const addressComponent = regeocode.addressComponent || {}
              if (poi?.name) {
                form.locationName = poi.name
              } else if (addressComponent.street) {
                form.locationName = addressComponent.street + (addressComponent.streetNumber || '')
              } else if (addressComponent.township) {
                form.locationName = addressComponent.township
              } else {
                form.locationName = regeocode.formattedAddress || '未知位置'
              }
              ElMessage.success('定位成功')
            } else {
              ElMessage.warning('已获取坐标，请手动输入地点名称')
            }
          })
        })
      } catch (error) {
        console.error('逆地理编码失败:', error)
        ElMessage.warning('定位成功，请手动输入地点名称')
      }
    },
    () => {
      ElMessage.error('定位失败，请手动输入坐标')
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  )
}

const handleFileChange = () => {
  // file changed
}

const submitCheckIn = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      let imageUrls: string[] = []
      if (fileList.value.length > 0) {
        const rawFiles = fileList.value
          .filter(f => f.raw)
          .map(f => f.raw as File)

        if (rawFiles.length > 0) {
          // 上传前压缩图片
          const compressedFiles = await Promise.all(
            rawFiles.map(f => compressImage(f))
          )
          const res = await uploadFiles(compressedFiles)
          imageUrls = res.data.urls
        }
      }

      await checkInStore.addCheckIn({
        locationName: form.locationName,
        longitude: parseFloat(form.longitude),
        latitude: parseFloat(form.latitude),
        content: form.content,
        images: imageUrls
      })

      ElMessage.success('打卡成功')
      visible.value = false
      resetForm()
      emit('success')
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  })
}

const resetForm = () => {
  form.locationName = ''
  form.longitude = ''
  form.latitude = ''
  form.content = ''
  fileList.value = []
}
</script>

<style scoped>
.checkin-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
}

.checkin-dialog :deep(.el-dialog__body) {
  padding: 16px 24px 20px;
}

.checkin-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 24px;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.locate-btn {
  background: linear-gradient(135deg, #FF85AB, #FB7299);
  border: none;
  color: white;
  font-weight: 500;
}

.locate-btn:hover {
  background: linear-gradient(135deg, #FB7299, #E85D88);
  animation: breathe 2s ease-in-out infinite;
}

@keyframes breathe {
  0%, 100% { box-shadow: 0 0 0 0 rgba(251, 114, 153, 0.4); }
  50% { box-shadow: 0 0 0 8px rgba(251, 114, 153, 0); }
}

:deep(.el-upload--picture-card) {
  width: 96px;
  height: 96px;
  border-radius: 12px;
  border: 2px dashed #E3E5E7;
  background: #F1F2F3;
  transition: all 0.2s ease;
}

:deep(.el-upload--picture-card:hover) {
  border-color: #FB7299;
  background: #FFF0F3;
}

:deep(.el-upload-list__item) {
  width: 96px;
  height: 96px;
  border-radius: 12px;
}
</style>

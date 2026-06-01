<template>
  <div class="min-h-screen bg-[#F1F2F3]">
    <div class="max-w-2xl mx-auto p-4">
      <div class="card-elevated p-6">
        <el-form :model="form" label-position="top">
          <el-form-item label="标题" required>
            <el-input v-model="form.title" placeholder="行程标题" maxlength="50" size="large" />
          </el-form-item>
          <el-form-item label="城市">
            <el-input v-model="form.city" placeholder="目的地" size="large" />
          </el-form-item>
          <el-form-item label="封面">
            <el-input v-model="form.coverImage" placeholder="封面图片URL" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="行程简介" />
          </el-form-item>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="开始日期">
                <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束日期">
                <el-date-picker v-model="form.endDate" type="date" placeholder="选择日期" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-switch v-model="form.isPublic" active-text="公开" />
          </el-form-item>
          <el-form-item>
            <div class="flex gap-3">
              <el-button type="primary" size="large" :loading="loading" :disabled="!form.title" @click="submit">创建</el-button>
              <el-button size="large" @click="$router.back()">取消</el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createTripPlan } from '@/api/trip-plan'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  title: '', city: '', coverImage: '', description: '',
  startDate: null as string | null, endDate: null as string | null,
  isPublic: false
})

const submit = async () => {
  loading.value = true
  try {
    const res = await createTripPlan(form as any)
    ElMessage.success('创建成功')
    router.push(`/trip-plans/${(res.data as any).id}`)
  } catch (error: any) {
    ElMessage.error(error?.message || '创建失败，请重试')
  }
  finally { loading.value = false }
}
</script>

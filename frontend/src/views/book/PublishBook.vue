<template>
  <div class="publish-book">
    <h2 class="page-title">发布商品</h2>

    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" @submit.prevent="handleSubmit">
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input v-model.number="form.price" type="number" step="0.01" min="0" placeholder="请输入价格" style="width: 200px" />
          <span style="margin-left: 8px; color: #999">元</span>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :max="999" />
          <span style="margin-left: 8px; color: #999">本</span>
        </el-form-item>
        <el-form-item label="交易地点" prop="locate">
          <el-input v-model="form.locate" placeholder="请输入交易地点" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="form.author" placeholder="请输入作者（选填）" />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="form.publisher" placeholder="请输入出版社（选填）" />
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="form.edition" placeholder="请输入版本（选填）" />
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
          <el-upload
            v-model:file-list="imageList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="3"
            accept="image/*"
            :on-exceed="handleExceed"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="color: #999; font-size: 12px; margin-top: 4px">最多上传 3 张图片</div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商品描述（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" native-type="submit">发布</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { publishBook } from '@/api/book'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const imageList = ref([])

const form = reactive({
  title: '',
  price: 1,
  quantity: 1,
  locate: '',
  author: '',
  publisher: '',
  edition: '',
  description: '',
})

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' },
  ],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  locate: [{ required: true, message: '请输入交易地点', trigger: 'blur' }],
  images: [
    {
      validator: (rule, value, callback) => {
        if (imageList.value.length === 0) {
          callback(new Error('请至少上传一张图片'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

function handleExceed() {
  ElMessage.warning('最多上传 3 张图片')
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const fd = new FormData()
    fd.append('title', form.title)
    fd.append('price', form.price)
    fd.append('quantity', form.quantity)
    fd.append('locate', form.locate)
    if (form.author) fd.append('author', form.author)
    if (form.publisher) fd.append('publisher', form.publisher)
    if (form.edition) fd.append('edition', form.edition)
    if (form.description) fd.append('description', form.description)
    imageList.value.forEach((file) => {
      fd.append('images', file.raw)
    })

    await publishBook(fd)
    ElMessage.success('发布成功')
    router.push('/books/my')
  } catch (e) {
    ElMessage.error(e.message || '发布失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.publish-book {
  max-width: 700px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}
</style>

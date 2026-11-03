<template>
  <div class="my-books">
    <h2 class="page-title">我的商品</h2>

    <el-table :data="books" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="locate" label="交易地点" width="140" show-overflow-tooltip />
      <el-table-column label="库存" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.quantity === 0" type="danger" size="small">已售罄</el-tag>
          <span v-else>{{ row.quantity }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" :disabled="row.quantity === 0" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" :disabled="row.quantity === 0" @click="handleDelist(row)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <EmptyState v-if="books.length === 0 && !loading" text="还没有发布过商品" />

    <el-dialog v-model="editVisible" title="编辑商品" width="600px">
      <el-form ref="editFormRef" :model="editForm" label-width="100px">
        <el-form-item label="书名">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model.number="editForm.price" type="number" step="0.01" min="0" placeholder="请输入价格" style="width: 200px" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="editForm.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="交易地点">
          <el-input v-model="editForm.locate" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="editForm.author" />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="editForm.publisher" />
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="editForm.edition" />
        </el-form-item>
        <el-form-item label="新图片">
          <el-upload
            v-model:file-list="editImageList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="3"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getMyBooks, updateBook, delistBook } from '@/api/book'
import { getBookDetail } from '@/api/book'
import EmptyState from '@/components/EmptyState.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const books = ref([])
const loading = ref(false)

const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editImageList = ref([])
const editBookId = ref(null)

const editForm = reactive({
  title: '',
  price: 0,
  quantity: 1,
  locate: '',
  author: '',
  publisher: '',
  edition: '',
  description: '',
})

onMounted(() => fetchBooks())

async function fetchBooks() {
  loading.value = true
  try {
    const data = await getMyBooks()
    books.value = Array.isArray(data) ? data : []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function handleEdit(row) {
  if (row.quantity === 0) {
    ElMessage.warning('已售罄的商品无法编辑')
    return
  }
  editBookId.value = row.id
  try {
    const detail = await getBookDetail(row.id)
    Object.assign(editForm, {
      title: detail.title,
      price: detail.price,
      quantity: detail.quantity,
      locate: detail.locate,
      author: detail.author || '',
      publisher: detail.publisher || '',
      edition: detail.edition || '',
      description: detail.description || '',
    })
    editImageList.value = []
    editVisible.value = true
  } catch (e) {
    ElMessage.error(e.message || '加载详情失败')
  }
}

async function handleSave() {
  editLoading.value = true
  try {
    const fd = new FormData()
    fd.append('title', editForm.title)
    fd.append('price', editForm.price)
    fd.append('quantity', editForm.quantity)
    fd.append('locate', editForm.locate)
    if (editForm.author) fd.append('author', editForm.author)
    if (editForm.publisher) fd.append('publisher', editForm.publisher)
    if (editForm.edition) fd.append('edition', editForm.edition)
    if (editForm.description) fd.append('description', editForm.description)
    editImageList.value.forEach((file) => {
      fd.append('images', file.raw)
    })

    await updateBook(editBookId.value, fd)
    ElMessage.success('保存成功')
    editVisible.value = false
    await fetchBooks()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    editLoading.value = false
  }
}

async function handleDelist(row) {
  if (row.quantity === 0) {
    ElMessage.warning('已售罄的商品无法下架')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要下架「${row.title}」吗？`, '确认下架', {
      confirmButtonText: '下架',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await delistBook(row.id)
    ElMessage.success('已下架')
    await fetchBooks()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '下架失败')
    }
  }
}
</script>

<style lang="scss" scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}
</style>

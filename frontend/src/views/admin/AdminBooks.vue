<template>
  <div class="admin-books">
    <h2 class="page-title">商品管理</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部商品" name="all">
        <el-table :data="allBooks" style="width: 100%" v-loading="allLoading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="locate" label="交易地点" width="140" show-overflow-tooltip />
          <el-table-column prop="sellerId" label="卖家ID" width="80" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="已删除商品" name="deleted">
        <el-table :data="deletedBooks" style="width: 100%" v-loading="deletedLoading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="sellerId" label="卖家ID" width="80" />
          <el-table-column label="有未完成订单" width="120">
            <template #default="{ row }">
              <el-tag :type="row.hasUnfinishedOrders ? 'danger' : 'success'" size="small">
                {{ row.hasUnfinishedOrders ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                type="danger"
                link
                size="small"
                :disabled="!row.canDelete"
                @click="handlePermanentDelete(row)"
              >
                永久删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBookList } from '@/api/book'
import { getDeletedBooks, permanentDeleteBook } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('all')

const allBooks = ref([])
const allLoading = ref(false)

const deletedBooks = ref([])
const deletedLoading = ref(false)

onMounted(() => fetchAllBooks())

async function fetchAllBooks() {
  allLoading.value = true
  try {
    const data = await getBookList()
    allBooks.value = Array.isArray(data) ? data : []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    allLoading.value = false
  }
}

async function fetchDeletedBooks() {
  deletedLoading.value = true
  try {
    const data = await getDeletedBooks()
    deletedBooks.value = Array.isArray(data) ? data : []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    deletedLoading.value = false
  }
}

function handleTabChange(tab) {
  if (tab === 'deleted' && deletedBooks.value.length === 0) {
    fetchDeletedBooks()
  }
}

async function handlePermanentDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要永久删除「${row.title}」吗？此操作不可恢复！`, '永久删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await permanentDeleteBook(row.id)
    ElMessage.success('已永久删除')
    await fetchDeletedBooks()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
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

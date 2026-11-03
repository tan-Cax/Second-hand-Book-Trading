<template>
  <div class="seller-orders">
    <h2 class="page-title">卖家订单</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待发货" name="1" />
      <el-tab-pane label="待收货" name="2" />
      <el-tab-pane label="已完成" name="3" />
      <el-tab-pane label="已取消" name="4" />
    </el-tabs>

    <el-table :data="orders" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="订单号" width="100" />
      <el-table-column prop="totalAmount" label="金额" width="120">
        <template #default="{ row }">
          <span style="font-weight: 600; color: #5B9BD5">¥{{ row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <OrderStatusTag :status="row.status" />
        </template>
      </el-table-column>
      <el-table-column prop="orderTime" label="下单时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" type="primary" link size="small" @click="handleShip(row)">发货</el-button>
          <el-button type="info" link size="small" @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <EmptyState v-if="orders.length === 0 && !loading" text="暂无订单" />

    <Pagination
      v-model:currentPage="page"
      :total="total"
      :pageSize="pageSize"
      @update:currentPage="fetchOrders"
    />

    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <div v-if="detail">
        <p><strong>订单号：</strong>{{ detail.id }}</p>
        <p><strong>总金额：</strong>¥{{ detail.totalAmount }}</p>
        <p><strong>状态：</strong><OrderStatusTag :status="detail.status" /></p>
        <p><strong>下单时间：</strong>{{ detail.orderTime }}</p>
        <el-table :data="detail.items" style="width: 100%; margin-top: 12px">
          <el-table-column prop="title" label="书名" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="小计" width="100">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSellerOrders, getOrderDetail, shipOrder } from '@/api/order'
import OrderStatusTag from '@/components/OrderStatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import Pagination from '@/components/Pagination.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const activeTab = ref('')
const page = ref(1)
const pageSize = 10
const total = ref(0)

const detailVisible = ref(false)
const detail = ref(null)

onMounted(() => fetchOrders())

async function fetchOrders() {
  loading.value = true
  try {
    const data = await getSellerOrders(page.value, pageSize)
    orders.value = data.records || []
    total.value = data.totalCount || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  page.value = 1
  fetchOrders()
}

async function handleShip(row) {
  try {
    await ElMessageBox.confirm(`确定要对订单 #${row.id} 标记为已发货吗？`, '确认发货', {
      confirmButtonText: '确认发货',
      cancelButtonText: '取消',
      type: 'info',
    })
    await shipOrder(row.id)
    ElMessage.success('已发货')
    await fetchOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '发货失败')
  }
}

async function handleDetail(row) {
  try {
    detail.value = await getOrderDetail(row.id)
    detailVisible.value = true
  } catch (e) {
    ElMessage.error(e.message || '加载详情失败')
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

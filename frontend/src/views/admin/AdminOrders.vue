<template>
  <div class="admin-orders">
    <h2 class="page-title">订单管理</h2>

    <div class="admin-orders__filter">
      <span>状态筛选：</span>
      <el-select v-model="statusFilter" placeholder="全部状态" clearable @change="handleFilter">
        <el-option label="全部" value="" />
        <el-option label="待付款" :value="0" />
        <el-option label="待发货" :value="1" />
        <el-option label="待收货" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已取消" :value="4" />
      </el-select>
    </div>

    <el-table :data="orders" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="订单号" width="100" />
      <el-table-column prop="buyerName" label="买家" width="120" />
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
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="info" link size="small" @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      v-model:currentPage="page"
      :total="total"
      :pageSize="pageSize"
      @update:currentPage="fetchOrders"
    />

    <el-dialog v-model="detailVisible" title="订单详情" width="650px">
      <div v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="买家">{{ detail.buyerName }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ detail.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="状态"><OrderStatusTag :status="detail.status" /></el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ detail.orderTime }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px">订单商品</h4>
        <el-table :data="detail.items" style="width: 100%">
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
import { getAdminOrders, getAdminOrderDetail } from '@/api/admin'
import OrderStatusTag from '@/components/OrderStatusTag.vue'
import Pagination from '@/components/Pagination.vue'
import { ElMessage } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const statusFilter = ref('')
const page = ref(1)
const pageSize = 20
const total = ref(0)

const detailVisible = ref(false)
const detail = ref(null)

onMounted(() => fetchOrders())

async function fetchOrders() {
  loading.value = true
  try {
    const data = await getAdminOrders(statusFilter.value, page.value, pageSize)
    orders.value = data.records || []
    total.value = data.totalCount || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  page.value = 1
  fetchOrders()
}

async function handleDetail(row) {
  try {
    detail.value = await getAdminOrderDetail(row.id)
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

.admin-orders {
  &__filter {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    font-size: 14px;
    color: #666;
  }
}
</style>

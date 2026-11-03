<template>
  <div class="my-orders">
    <h2 class="page-title">我的订单</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待付款" name="0" />
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
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" link size="small" @click="handlePay(row)">付款</el-button>
          <el-button v-if="row.status === 2" type="success" link size="small" @click="handleConfirm(row)">确认收货</el-button>
          <el-button v-if="row.status === 0" type="danger" link size="small" @click="handleCancel(row)">取消</el-button>
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

    <el-dialog v-model="payVisible" title="确认付款" width="380px">
      <el-form-item label="支付密码">
        <el-input v-model="payPassword" type="password" placeholder="请输入支付密码" show-password />
      </el-form-item>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" :loading="payLoading" @click="confirmPay">确认付款</el-button>
      </template>
    </el-dialog>

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
import { getMyOrders, getOrderDetail, payOrder, confirmOrder, cancelOrder } from '@/api/order'
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

const payVisible = ref(false)
const payLoading = ref(false)
const payPassword = ref('')
const payOrderId = ref(null)

const detailVisible = ref(false)
const detail = ref(null)

onMounted(() => fetchOrders())

async function fetchOrders() {
  loading.value = true
  try {
    const data = await getMyOrders(page.value, pageSize, activeTab.value)
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

function handlePay(row) {
  payOrderId.value = row.id
  payPassword.value = ''
  payVisible.value = true
}

async function confirmPay() {
  if (!payPassword.value) {
    ElMessage.warning('请输入支付密码')
    return
  }
  payLoading.value = true
  try {
    await payOrder(payOrderId.value, { payPassword: payPassword.value })
    ElMessage.success('付款成功')
    payVisible.value = false
    await fetchOrders()
  } catch (e) {
    ElMessage.error(e.message || '付款失败')
  } finally {
    payLoading.value = false
  }
}

async function handleConfirm(row) {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info',
    })
    await confirmOrder(row.id)
    ElMessage.success('已确认收货')
    await fetchOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '返回',
      type: 'warning',
    })
    await cancelOrder(row.id)
    ElMessage.success('订单已取消')
    await fetchOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '取消失败')
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

<template>
  <div class="admin-funds">
    <h2 class="page-title">资金管理</h2>

    <div class="admin-funds__stats" v-if="statistics">
      <div class="stat-card">
        <div class="stat-card__value">{{ statistics.totalUsers }}</div>
        <div class="stat-card__label">用户总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-card__value">{{ statistics.totalOrders }}</div>
        <div class="stat-card__label">订单总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-card__value">{{ statistics.totalWallets }}</div>
        <div class="stat-card__label">钱包总数</div>
      </div>
      <div class="stat-card stat-card--primary">
        <div class="stat-card__value">¥{{ statistics.totalRecharge }}</div>
        <div class="stat-card__label">充值总额</div>
      </div>
      <div class="stat-card stat-card--success">
        <div class="stat-card__value">¥{{ statistics.totalSales }}</div>
        <div class="stat-card__label">销售总额</div>
      </div>
      <div class="stat-card stat-card--danger">
        <div class="stat-card__value">¥{{ statistics.totalRefund }}</div>
        <div class="stat-card__label">退款总额</div>
      </div>
    </div>

    <el-card class="admin-funds__order-stats" v-if="statistics?.ordersByStatus">
      <template #header><span>各状态订单数</span></template>
      <div class="status-grid">
        <div class="status-item" v-for="(count, status) in statistics.ordersByStatus" :key="status">
          <span class="status-item__label">{{ statusText(status) }}</span>
          <span class="status-item__value">{{ count }}</span>
        </div>
      </div>
    </el-card>

    <el-card class="admin-funds__log">
      <template #header><span>交易记录</span></template>

      <div class="admin-funds__filter">
        <el-input v-model="walletIdFilter" placeholder="钱包ID" clearable style="width: 140px" />
        <el-select v-model="typeFilter" placeholder="交易类型" clearable style="width: 140px">
          <el-option label="全部" value="" />
          <el-option label="充值" :value="1" />
          <el-option label="购买" :value="2" />
          <el-option label="销售" :value="3" />
          <el-option label="退款" :value="4" />
        </el-select>
        <el-button type="primary" @click="handleFilter">筛选</el-button>
      </div>

      <el-table :data="transactions" style="width: 100%" v-loading="txLoading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.amount >= 0 ? '#5CB85C' : '#E65C5C' }">
              {{ row.amount >= 0 ? '+' : '' }}¥{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="typeText" label="类型" width="100" />
        <el-table-column prop="orderId" label="关联订单" width="100">
          <template #default="{ row }">{{ row.orderId ? '#' + row.orderId : '-' }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdTime" label="时间" width="180" />
      </el-table>

      <Pagination
        v-model:currentPage="txPage"
        :total="txTotal"
        :pageSize="txPageSize"
        @update:currentPage="fetchTransactions"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminStatistics, getAdminTransactions } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'
import { ElMessage } from 'element-plus'

const statistics = ref(null)

const transactions = ref([])
const txLoading = ref(false)
const txPage = ref(1)
const txPageSize = 20
const txTotal = ref(0)
const walletIdFilter = ref('')
const typeFilter = ref('')

const statusMap = {
  0: '待付款',
  1: '待发货',
  2: '待收货',
  3: '已完成',
  4: '已取消',
}

function statusText(status) {
  return statusMap[status] || status
}

onMounted(async () => {
  await Promise.all([fetchStats(), fetchTransactions()])
})

async function fetchStats() {
  try {
    statistics.value = await getAdminStatistics()
  } catch (e) {
    ElMessage.error(e.message || '加载统计失败')
  }
}

async function fetchTransactions() {
  txLoading.value = true
  try {
    const data = await getAdminTransactions(walletIdFilter.value, typeFilter.value, txPage.value, txPageSize)
    transactions.value = data.records || []
    txTotal.value = data.totalCount || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    txLoading.value = false
  }
}

function handleFilter() {
  txPage.value = 1
  fetchTransactions()
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.admin-funds {
  &__stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 20px;
  }

  &__order-stats {
    margin-bottom: 20px;
  }

  &__filter {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
  }

  &__log {
    margin-top: 20px;
  }
}

.stat-card {
  background: $bg-white;
  border-radius: 6px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

  &__value {
    font-size: 24px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 4px;
  }

  &__label {
    font-size: 13px;
    color: $text-light;
  }

  &--primary .stat-card__value {
    color: $primary-color;
  }

  &--success .stat-card__value {
    color: $success-color;
  }

  &--danger .stat-card__value {
    color: $danger-color;
  }
}

.status-grid {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;

  &__label {
    font-size: 13px;
    color: $text-light;
  }

  &__value {
    font-size: 20px;
    font-weight: 600;
    color: $text-primary;
  }
}
</style>

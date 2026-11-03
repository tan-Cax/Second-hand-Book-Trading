<template>
  <div class="wallet">
    <h2 class="page-title">我的钱包</h2>

    <el-card class="wallet__info" v-if="walletStore.loaded">
      <div class="wallet__balance">
        <span class="wallet__balance-label">余额</span>
        <span class="wallet__balance-value">¥{{ walletStore.balance }}</span>
      </div>
      <div class="wallet__actions">
        <el-button type="primary" @click="rechargeVisible = true">充值</el-button>
        <el-button v-if="!walletStore.walletId" type="success" @click="createWalletVisible = true">开通钱包</el-button>
        <el-button v-else @click="payPwdVisible = true">设置支付密码</el-button>
      </div>
    </el-card>

    <el-card v-else-if="!walletStore.walletId" class="wallet__create-tip">
      <el-empty description="您还没有开通钱包">
        <el-button type="primary" @click="createWalletVisible = true">立即开通</el-button>
      </el-empty>
    </el-card>

    <el-card class="wallet__log">
      <template #header>
        <span>交易记录</span>
      </template>
      <el-table :data="logs" style="width: 100%" v-loading="logLoading">
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.amount >= 0 ? '#5CB85C' : '#E65C5C' }">
              {{ row.amount >= 0 ? '+' : '' }}¥{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="typeText" label="类型" width="100" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="orderId" label="关联订单" width="100">
          <template #default="{ row }">
            {{ row.orderId ? '#' + row.orderId : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="时间" width="180" />
      </el-table>
      <Pagination
        v-model:currentPage="logPage"
        :total="logTotal"
        :pageSize="logPageSize"
        @update:currentPage="fetchLogs"
      />
    </el-card>

    <el-dialog v-model="rechargeVisible" title="充值" width="400px">
      <el-form label-width="80px">
        <el-form-item label="金额">
          <el-input-number v-model="rechargeForm.amount" :min="1" :precision="2" />
          <span style="margin-left: 8px; color: #999">元</span>
        </el-form-item>
        <el-form-item label="支付密码">
          <el-input v-model="rechargeForm.payPassword" type="password" show-password placeholder="请输入支付密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="rechargeLoading" @click="handleRecharge">确认充值</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="createWalletVisible" title="开通钱包" width="400px">
      <el-form label-width="100px">
        <el-form-item label="设置支付密码">
          <el-input v-model="createForm.payPassword" type="password" show-password placeholder="请设置6位以上支付密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createWalletVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreate">确认开通</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="payPwdVisible" title="设置支付密码" width="400px">
      <el-form label-width="100px">
        <el-form-item label="新支付密码">
          <el-input v-model="newPayPwd" type="password" show-password placeholder="请输入新支付密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payPwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="payPwdLoading" @click="handleSetPayPwd">确认设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useWalletStore } from '@/stores/wallet'
import { recharge, createWallet, setPayPassword, getTransactionLog } from '@/api/wallet'
import Pagination from '@/components/Pagination.vue'
import { ElMessage } from 'element-plus'

const walletStore = useWalletStore()

const logs = ref([])
const logLoading = ref(false)
const logPage = ref(1)
const logPageSize = 15
const logTotal = ref(0)

const rechargeVisible = ref(false)
const rechargeLoading = ref(false)
const rechargeForm = reactive({ amount: 10, payPassword: '' })

const createWalletVisible = ref(false)
const createLoading = ref(false)
const createForm = reactive({ payPassword: '' })

const payPwdVisible = ref(false)
const payPwdLoading = ref(false)
const newPayPwd = ref('')

onMounted(async () => {
  await walletStore.fetch()
  if (walletStore.walletId) {
    await fetchLogs()
  }
})

async function fetchLogs() {
  logLoading.value = true
  try {
    const data = await getTransactionLog(logPage.value, logPageSize)
    logs.value = data.records || []
    logTotal.value = data.totalCount || 0
  } catch {
    logs.value = []
  } finally {
    logLoading.value = false
  }
}

async function handleRecharge() {
  rechargeLoading.value = true
  try {
    await recharge({ amount: rechargeForm.amount, payPassword: rechargeForm.payPassword })
    ElMessage.success('充值成功')
    rechargeVisible.value = false
    rechargeForm.payPassword = ''
    await walletStore.fetch()
    await fetchLogs()
  } catch (e) {
    ElMessage.error(e.message || '充值失败')
  } finally {
    rechargeLoading.value = false
  }
}

async function handleCreate() {
  createLoading.value = true
  try {
    await createWallet({ payPassword: createForm.payPassword })
    ElMessage.success('钱包开通成功')
    createWalletVisible.value = false
    createForm.payPassword = ''
    await walletStore.fetch()
    await fetchLogs()
  } catch (e) {
    ElMessage.error(e.message || '开通失败')
  } finally {
    createLoading.value = false
  }
}

async function handleSetPayPwd() {
  payPwdLoading.value = true
  try {
    await setPayPassword({ payPassword: newPayPwd.value })
    ElMessage.success('支付密码设置成功')
    payPwdVisible.value = false
    newPayPwd.value = ''
  } catch (e) {
    ElMessage.error(e.message || '设置失败')
  } finally {
    payPwdLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.wallet {
  &__info {
    margin-bottom: 20px;
  }

  &__balance {
    display: flex;
    align-items: baseline;
    gap: 16px;
    margin-bottom: 16px;
  }

  &__balance-label {
    font-size: 14px;
    color: $text-secondary;
  }

  &__balance-value {
    font-size: 32px;
    font-weight: 600;
    color: $primary-color;
  }

  &__actions {
    display: flex;
    gap: 12px;
  }

  &__create-tip {
    margin-bottom: 20px;
  }

  &__log {
    margin-top: 20px;
  }
}
</style>

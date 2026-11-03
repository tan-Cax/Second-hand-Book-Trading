<template>
  <div class="admin-users">
    <h2 class="page-title">用户管理</h2>

    <div class="admin-users__search">
      <el-input v-model="keyword" placeholder="搜索用户名" clearable @keyup.enter="handleSearch" style="width: 260px" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="keyword = ''; fetchUsers()">重置</el-button>
    </div>

    <el-table :data="users" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="用户名" width="140" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : ''" size="small">
            {{ row.role === 1 ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isBanned ? 'danger' : 'success'" size="small">
            {{ row.isBanned ? '已封禁' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.payLocked" type="warning" size="small">已冻结</el-tag>
          <el-tag v-else type="success" size="small">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="注册时间" width="180" />
      <el-table-column label="操作" min-width="240" fixed="right">
        <template #default="{ row }">
          <template v-if="row.role !== 1">
            <el-button
              v-if="!row.isBanned"
              type="danger"
              link
              size="small"
              @click="handleBan(row)"
            >封禁</el-button>
            <el-button
              v-else
              type="success"
              link
              size="small"
              @click="handleUnban(row)"
            >解封</el-button>
            <el-button type="warning" link size="small" @click="handleLockPay(row)">锁定支付</el-button>
            <el-button type="info" link size="small" @click="handleUnlockPay(row)">解锁支付</el-button>
          </template>
          <span v-else style="color: #999">-</span>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      v-model:currentPage="page"
      :total="total"
      :pageSize="pageSize"
      @update:currentPage="fetchUsers"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminUsers, banUser, unbanUser, lockPay, unlockPay } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const keyword = ref('')
const users = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)

onMounted(() => fetchUsers())

async function fetchUsers() {
  loading.value = true
  try {
    const data = await getAdminUsers(keyword.value, page.value, pageSize)
    users.value = data.records || []
    total.value = data.totalCount || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchUsers()
}

async function handleBan(row) {
  try {
    await ElMessageBox.confirm(`确定要封禁用户「${row.name}」吗？将取消其所有未完成订单并下架其商品。`, '封禁用户', {
      confirmButtonText: '封禁',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await banUser(row.id)
    ElMessage.success('已封禁')
    await fetchUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '封禁失败')
  }
}

async function handleUnban(row) {
  try {
    await ElMessageBox.confirm(`确定要解封用户「${row.name}」吗？`, '解封用户', {
      confirmButtonText: '解封',
      cancelButtonText: '取消',
      type: 'info',
    })
    await unbanUser(row.id)
    ElMessage.success('已解封')
    await fetchUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '解封失败')
  }
}

async function handleLockPay(row) {
  try {
    await ElMessageBox.confirm(`确定要锁定用户「${row.name}」的支付功能吗？`, '锁定支付', {
      confirmButtonText: '锁定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await lockPay(row.id)
    ElMessage.success('已锁定支付')
    await fetchUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '锁定失败')
  }
}

async function handleUnlockPay(row) {
  try {
    await ElMessageBox.confirm(`确定要解锁用户「${row.name}」的支付功能吗？`, '解锁支付', {
      confirmButtonText: '解锁',
      cancelButtonText: '取消',
      type: 'info',
    })
    await unlockPay(row.id)
    ElMessage.success('已解锁支付')
    await fetchUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '解锁失败')
  }
}
</script>

<style lang="scss" scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.admin-users {
  &__search {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
  }
}
</style>

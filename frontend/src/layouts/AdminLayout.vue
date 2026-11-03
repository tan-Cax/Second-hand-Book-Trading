<template>
  <div class="admin-layout">
    <header class="admin-layout__header">
      <div class="admin-layout__logo">二手书交易 · 管理后台</div>
      <div class="admin-layout__user">
        <span class="admin-layout__username">管理员: {{ authStore.user?.name }}</span>
        <el-button text style="color: #fff; border-radius: 4px" @click="handleLogout">退出登录</el-button>
      </div>
    </header>
    <div class="admin-layout__body">
      <aside class="admin-layout__sidebar">
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#3A4A5C"
          text-color="#B0BEC5"
          active-text-color="#A3CAEF"
        >
          <el-menu-item index="/admin/books">
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/funds">
            <span>资金管理</span>
          </el-menu-item>
        </el-menu>
      </aside>
      <main class="admin-layout__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logoutApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)

function handleLogout() {
  logoutApi().catch(() => {})
  authStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.admin-layout {
  min-height: 100vh;

  &__header {
    height: 56px;
    background: #3A4A5C;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    color: #fff;
  }

  &__logo {
    font-size: 18px;
    font-weight: 600;
  }

  &__user {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__username {
    font-size: 14px;
    color: #B0BEC5;
  }

  &__body {
    display: flex;
    min-height: calc(100vh - 56px);
  }

  &__sidebar {
    width: 200px;
    background: #3A4A5C;

    :deep(.el-menu) {
      border-right: none;
    }

    :deep(.el-menu-item) {
      &:hover {
        background-color: rgba(255, 255, 255, 0.08) !important;
      }

      &.is-active {
        background-color: rgba(91, 155, 213, 0.2) !important;
      }
    }
  }

  &__content {
    flex: 1;
    padding: 20px 24px;
    background: $bg-page;
    overflow-y: auto;
  }
}
</style>

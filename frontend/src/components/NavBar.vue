<template>
  <header class="navbar">
    <div class="navbar__inner">
      <router-link to="/home" class="navbar__logo">二手书交易</router-link>

      <nav class="navbar__nav">
        <router-link to="/home" class="navbar__link">首页</router-link>
        <router-link to="/books/publish" class="navbar__link">发布商品</router-link>
        <router-link to="/books/my" class="navbar__link">我的商品</router-link>
        <router-link to="/seller/orders" class="navbar__link">卖家订单</router-link>
        <router-link to="/orders" class="navbar__link">我的订单</router-link>
      </nav>

      <div class="navbar__right">
        <router-link to="/cart" class="navbar__cart">
          购物车
          <span v-if="cartStore.count > 0" class="navbar__badge">{{ cartStore.count }}</span>
        </router-link>

        <el-dropdown trigger="click" @command="handleCommand">
          <span class="navbar__user">
            {{ authStore.user?.name || '用户' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="wallet">我的钱包</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </header>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { logoutApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

onMounted(() => {
  cartStore.fetch()
})

function handleCommand(cmd) {
  switch (cmd) {
    case 'profile':
      router.push('/profile')
      break
    case 'wallet':
      router.push('/wallet')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'logout':
      logoutApi().catch(() => {})
      authStore.logout()
      router.push('/login')
      break
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.navbar {
  height: $nav-height;
  background: $primary-color;
  position: sticky;
  top: 0;
  z-index: 100;

  &__inner {
    max-width: 1200px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    padding: 0 16px;
  }

  &__logo {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
    text-decoration: none;
    margin-right: 32px;
    white-space: nowrap;
  }

  &__nav {
    display: flex;
    gap: 4px;
  }

  &__link {
    color: rgba(255, 255, 255, 0.85);
    text-decoration: none;
    padding: 6px 14px;
    border-radius: 4px;
    font-size: 14px;
    transition: background 0.2s;

    &:hover,
    &.router-link-active {
      background: rgba(255, 255, 255, 0.15);
      color: #fff;
    }
  }

  &__right {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__cart {
    color: rgba(255, 255, 255, 0.9);
    text-decoration: none;
    font-size: 14px;
    position: relative;
    padding: 6px 14px;
    border-radius: 4px;
    transition: background 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.15);
    }
  }

  &__badge {
    position: absolute;
    top: 0;
    right: 0;
    background: $danger-color;
    color: #fff;
    font-size: 11px;
    min-width: 16px;
    height: 16px;
    line-height: 16px;
    text-align: center;
    border-radius: 8px;
    padding: 0 4px;
  }

  &__user {
    color: #fff;
    font-size: 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 14px;
    border-radius: 4px;
    transition: background 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.15);
    }
  }
}
</style>

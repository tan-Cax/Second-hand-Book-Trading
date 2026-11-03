<template>
  <div class="user-layout">
    <NavBar />
    <main class="user-layout__main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import NavBar from '@/components/NavBar.vue'
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.user-layout {
  min-height: 100vh;
  background: $bg-page;

  &__main {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px 16px;
  }
}
</style>

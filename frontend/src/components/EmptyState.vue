<template>
  <div class="empty-state">
    <div class="empty-state__icon">{{ icon }}</div>
    <div class="empty-state__text">{{ text }}</div>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '暂无数据' },
  icon: { type: String, default: '📦' },
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;

  &__icon {
    font-size: 48px;
    margin-bottom: 12px;
  }

  &__text {
    font-size: 14px;
    color: $text-light;
  }
}
</style>

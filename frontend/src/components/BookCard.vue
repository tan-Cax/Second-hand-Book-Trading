<template>
  <div class="book-card" @click="goDetail">
    <div class="book-card__image">
      <img v-if="imageUrl" :src="imageUrl" :alt="title" />
      <div v-else class="book-card__placeholder">暂无图片</div>
    </div>
    <div class="book-card__info">
      <div class="book-card__title" :title="title">{{ title }}</div>
      <div class="book-card__price">¥{{ price }}</div>
      <div class="book-card__locate">{{ locate }}</div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  id: { type: Number, required: true },
  title: { type: String, default: '' },
  price: { type: [Number, String], default: 0 },
  locate: { type: String, default: '' },
  imageUrl: { type: String, default: '' },
})

const router = useRouter()

function goDetail() {
  router.push(`/book/${props.id}`)
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.book-card {
  background: $bg-white;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }

  &__image {
    height: 180px;
    background: $primary-bg;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__placeholder {
    font-size: 13px;
    color: $text-light;
  }

  &__info {
    padding: 10px 12px;
  }

  &__title {
    font-size: 14px;
    color: $text-primary;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__price {
    font-size: 16px;
    font-weight: 600;
    color: $primary-color;
    margin: 6px 0 4px;
  }

  &__locate {
    font-size: 12px;
    color: $text-light;
  }
}
</style>

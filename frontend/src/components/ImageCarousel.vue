<template>
  <div class="image-carousel">
    <div class="image-carousel__main">
      <img :src="images[currentIndex]?.imageUrl || placeholder" class="image-carousel__img" />
    </div>
    <div v-if="images.length > 1" class="image-carousel__thumbs">
      <div
        v-for="(img, idx) in images"
        :key="img.id || idx"
        class="image-carousel__thumb"
        :class="{ 'is-active': idx === currentIndex }"
        @click="currentIndex = idx"
      >
        <img :src="img.imageUrl" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  images: { type: Array, default: () => [] },
  placeholder: { type: String, default: '' },
})

const currentIndex = ref(0)
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.image-carousel {
  &__main {
    width: 100%;
    aspect-ratio: 4 / 3;
    background: $primary-bg;
    border-radius: 4px;
    overflow: hidden;
    margin-bottom: 10px;
  }

  &__img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &__thumbs {
    display: flex;
    gap: 8px;
  }

  &__thumb {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: border-color 0.2s;

    &.is-active {
      border-color: $primary-color;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}
</style>

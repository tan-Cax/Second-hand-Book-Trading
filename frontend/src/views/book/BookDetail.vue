<template>
  <div class="book-detail">
    <el-button text @click="$router.back()" style="margin-bottom: 12px">← 返回</el-button>

    <div v-if="book" class="book-detail__main">
      <div class="book-detail__left">
        <ImageCarousel :images="book.images || []" />
      </div>

      <div class="book-detail__right">
        <h1 class="book-detail__title">{{ book.title }}</h1>

        <div class="book-detail__info">
          <div v-if="book.author" class="book-detail__row">
            <span class="book-detail__label">作者</span>
            <span>{{ book.author }}</span>
          </div>
          <div v-if="book.publisher" class="book-detail__row">
            <span class="book-detail__label">出版社</span>
            <span>{{ book.publisher }}</span>
          </div>
          <div v-if="book.edition" class="book-detail__row">
            <span class="book-detail__label">版本</span>
            <span>{{ book.edition }}</span>
          </div>
          <div class="book-detail__row">
            <span class="book-detail__label">价格</span>
            <span class="book-detail__price">¥{{ book.price }}</span>
          </div>
          <div class="book-detail__row">
            <span class="book-detail__label">库存</span>
            <span>{{ book.quantity > 0 ? book.quantity + ' 本' : '已售罄' }}</span>
          </div>
          <div class="book-detail__row">
            <span class="book-detail__label">交易地点</span>
            <span>{{ book.locate }}</span>
          </div>
          <div class="book-detail__row">
            <span class="book-detail__label">发布时间</span>
            <span>{{ book.createdTime }}</span>
          </div>
        </div>

        <div v-if="book.quantity > 0" class="book-detail__action">
          <div class="book-detail__qty">
            <span class="book-detail__label">购买数量</span>
            <el-input-number v-model="buyQuantity" :min="1" :max="book.quantity" />
          </div>
          <div class="book-detail__buttons">
            <el-button type="primary" size="large" @click="handleAddCart">加入购物车</el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="book?.description" class="book-detail__desc">
      <h3>商品描述</h3>
      <p>{{ book.description }}</p>
    </div>

    <EmptyState v-if="!book && !loading" text="商品不存在或已下架" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getBookDetail } from '@/api/book'
import { useCartStore } from '@/stores/cart'
import ImageCarousel from '@/components/ImageCarousel.vue'
import EmptyState from '@/components/EmptyState.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const cartStore = useCartStore()

const book = ref(null)
const loading = ref(false)
const buyQuantity = ref(1)

onMounted(async () => {
  loading.value = true
  try {
    book.value = await getBookDetail(route.params.id)
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
})

async function handleAddCart() {
  try {
    await cartStore.add(book.value.id, buyQuantity.value)
    ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error(e.message || '加入购物车失败')
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.book-detail {
  &__main {
    display: flex;
    gap: 32px;
    background: $bg-white;
    border-radius: 6px;
    padding: 24px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  }

  &__left {
    width: 400px;
    flex-shrink: 0;
  }

  &__right {
    flex: 1;
  }

  &__title {
    font-size: 22px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 20px;
  }

  &__info {
    margin-bottom: 24px;
  }

  &__row {
    display: flex;
    align-items: center;
    padding: 8px 0;
    font-size: 14px;
    color: $text-secondary;
    border-bottom: 1px solid $border-color;

    &:last-child {
      border-bottom: none;
    }
  }

  &__label {
    width: 80px;
    color: $text-light;
    flex-shrink: 0;
  }

  &__price {
    font-size: 24px;
    font-weight: 600;
    color: $primary-color;
  }

  &__action {
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid $border-color;
  }

  &__qty {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
  }

  &__buttons {
    display: flex;
    gap: 12px;
  }

  &__desc {
    margin-top: 20px;
    background: $bg-white;
    border-radius: 6px;
    padding: 20px 24px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

    h3 {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 12px;
    }

    p {
      font-size: 14px;
      color: $text-secondary;
      line-height: 1.8;
      white-space: pre-wrap;
    }
  }
}
</style>

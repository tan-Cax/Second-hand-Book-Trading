<template>
  <div class="cart">
    <h2 class="page-title">我的购物车</h2>

    <el-card v-if="cartStore.items.length > 0">
      <el-table
        :data="cartStore.items"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <img v-if="row.imageUrl" :src="row.imageUrl" class="cart__thumb" />
            <div v-else class="cart__thumb-placeholder">无图</div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            <span class="cart__price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="140">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="99"
              size="small"
              @change="(val) => handleQuantityChange(row.bookId, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="100">
          <template #default="{ row }">
            <span class="cart__subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleRemove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart__footer">
        <div class="cart__footer-left">
          <el-button @click="handleClear">清空购物车</el-button>
        </div>
        <div class="cart__footer-right">
          <span class="cart__total">
            已选 {{ selectedItems.length }} 件，合计：
            <span class="cart__total-price">¥{{ selectedTotal.toFixed(2) }}</span>
          </span>
          <el-button type="primary" size="large" :disabled="selectedItems.length === 0" @click="handleCheckout">
            去结算
          </el-button>
        </div>
      </div>
    </el-card>

    <EmptyState v-else text="购物车是空的" icon="🛒" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { createOrder } from '@/api/order'
import EmptyState from '@/components/EmptyState.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

const selectedItems = ref([])

const selectedTotal = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

onMounted(() => cartStore.fetch())

function handleSelectionChange(rows) {
  selectedItems.value = rows
}

async function handleQuantityChange(bookId, quantity) {
  try {
    await cartStore.update(bookId, quantity)
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
    await cartStore.fetch()
  }
}

async function handleRemove(row) {
  try {
    await cartStore.remove(row.bookId)
    ElMessage.success('已移除')
  } catch (e) {
    ElMessage.error(e.message || '移除失败')
  }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '确认', {
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await cartStore.clear()
    ElMessage.success('购物车已清空')
  } catch {
    // cancelled
  }
}

async function handleCheckout() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认结算 ${selectedItems.value.length} 件商品，合计 ¥${selectedTotal.value.toFixed(2)}？`,
      '确认结算',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
    await createOrder(selectedItems.value.map(i => i.bookId))
    ElMessage.success('订单创建成功')
    await cartStore.fetch()
    router.push('/orders')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '创建订单失败')
    }
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

.cart {
  &__thumb {
    width: 56px;
    height: 56px;
    object-fit: cover;
    border-radius: 4px;
  }

  &__thumb-placeholder {
    width: 56px;
    height: 56px;
    background: $primary-bg;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: $text-light;
  }

  &__price {
    font-weight: 500;
  }

  &__subtotal {
    font-weight: 600;
    color: $primary-color;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid $border-color;
  }

  &__footer-right {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__total {
    font-size: 14px;
    color: $text-secondary;
  }

  &__total-price {
    font-size: 20px;
    font-weight: 600;
    color: $primary-color;
  }
}
</style>

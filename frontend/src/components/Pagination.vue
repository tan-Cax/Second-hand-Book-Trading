<template>
  <div v-if="total > 0" class="pagination">
    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      background
      @current-change="handleChange"
    />
  </div>
</template>

<script setup>
defineProps({
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
})

const emit = defineEmits(['update:currentPage'])

function handleChange(page) {
  emit('update:currentPage', page)
}
</script>

<style lang="scss" scoped>
.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;

  :deep(.el-pager li) {
    border-radius: 4px;
  }

  :deep(.el-pagination button) {
    border-radius: 4px;
  }
}
</style>

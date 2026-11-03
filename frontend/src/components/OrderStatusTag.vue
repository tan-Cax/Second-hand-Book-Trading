<template>
  <el-tag :type="tagType" size="small">{{ label }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: Number, required: true },
})

const statusMap = {
  0: { label: '待付款', type: 'warning' },
  1: { label: '待发货', type: 'info' },
  2: { label: '待收货', type: '' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'danger' },
}

const label = computed(() => statusMap[props.status]?.label || '未知')
const tagType = computed(() => statusMap[props.status]?.type || 'info')
</script>

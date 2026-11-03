<template>
  <router-view />
</template>

<style lang="scss">
@use '@/styles/variables' as *;

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  background: $bg-page;
  color: $text-primary;
}

.el-button {
  border-radius: 4px !important;
}

.el-input__wrapper,
.el-select .el-input__wrapper {
  border-radius: 4px !important;
}

.el-tag {
  border-radius: 3px !important;
}

.el-dialog {
  border-radius: 6px !important;
}

.el-table {
  border-radius: 4px !important;
  overflow: hidden;
}

.el-tabs__item.is-active {
  color: $primary-color !important;
}

.el-tabs__active-bar {
  background-color: $primary-color !important;
}
</style>

<template>
  <div class="content-card p-5 text-center">
    <div class="display-6 mb-3">暂无数据</div>
    <p class="text-muted mb-0">{{ text }}</p>
  </div>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: '当前没有可展示的数据'
  }
})
</script>

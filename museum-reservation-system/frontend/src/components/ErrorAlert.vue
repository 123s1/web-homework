<template>
  <div v-if="message" class="alert alert-danger rounded-4" role="alert">
    {{ message }}
  </div>
</template>

<script setup>
defineProps({
  message: {
    type: String,
    default: ''
  }
})
</script>

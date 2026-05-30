<template>
  <img v-if="dataUrl" :src="dataUrl" :width="size" :height="size" alt="预约凭证二维码" />
  <div v-else class="text-muted small">二维码生成中...</div>
</template>

<script setup>
import { ref, watch } from 'vue'
import QRCode from 'qrcode'

const props = defineProps({
  value: {
    type: String,
    default: ''
  },
  size: {
    type: Number,
    default: 220
  }
})

const dataUrl = ref('')

async function render(value) {
  if (!value) {
    dataUrl.value = ''
    return
  }
  try {
    dataUrl.value = await QRCode.toDataURL(value, {
      width: props.size,
      margin: 1,
      errorCorrectionLevel: 'M'
    })
  } catch (error) {
    dataUrl.value = ''
  }
}

watch(() => props.value, render, { immediate: true })
</script>

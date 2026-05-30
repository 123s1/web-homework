<template>
  <div class="app-shell">
    <AppNavbar />
    <main class="main-content">
      <slot />
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import AppFooter from '../components/AppFooter.vue'
import AppNavbar from '../components/AppNavbar.vue'
</script>

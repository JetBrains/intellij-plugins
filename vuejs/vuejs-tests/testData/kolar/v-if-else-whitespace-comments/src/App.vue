<script setup lang="ts">
const status = 'loading'
</script>

<template>
  <div v-if="status === 'loading'">Loading</div>

  <!-- transitional comment -->

  <div v-else-if="status === 'error'">Error</div>

  <!-- another comment -->

  <div v-else>Ready</div>
</template>

<script setup lang="ts">
const status = 'loading'
</script>

<template>
  <div v-if="status === 'loading'">Loading</div>
  <div v-else-if="status === 'error'">Error</div>
  <div v-else-if="status === 'empty'">Empty</div>
  <div v-else>Ready</div>
</template>

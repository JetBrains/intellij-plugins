<script setup lang="ts">
const flags: Record<string, boolean> = { beta: true }
const key = 'beta'
</script>

<template>
  <div v-if="flags[key]">Beta feature enabled</div>
</template>

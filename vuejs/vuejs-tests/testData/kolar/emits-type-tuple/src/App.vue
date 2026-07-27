<script setup lang="ts">
defineEmits<{
  change: [id: number]
  reset: []
}>()
</script>

<template>
  <button @click="$emit('change', 1)">Change</button>
  <button @click="$emit('reset')">Reset</button>
</template>

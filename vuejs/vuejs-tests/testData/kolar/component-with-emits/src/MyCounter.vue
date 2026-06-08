<script setup lang="ts">
const count = 1

defineEmits<{
  start: [],
  change: [{ value: number }],
  reset: [],
}>()
</script>

<template>
  {{ count }}

  <button @click="emit('start')">Start</button>
  <button @click="emit('change', {value: count})">Change</button>
  <button @click="emit('reset')">Reset</button>
</template>

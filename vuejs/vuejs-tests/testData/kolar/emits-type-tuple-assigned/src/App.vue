<script setup lang="ts">
const emit = defineEmits<{
  change: [id: number]
  reset: []
}>()

function onChange() {
  emit('change', 1)
}
</script>

<template>
  <button @click="onChange">Change</button>
  <button @click="emit('change', 1)">Change (direct)</button>
  <button @click="emit('reset')">Reset</button>
</template>

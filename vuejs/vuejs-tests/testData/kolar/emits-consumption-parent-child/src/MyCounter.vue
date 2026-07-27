<script setup lang="ts">
const count = 1

const emit = defineEmits<{
  change: [id: number]
  reset: []
}>()

function onChange() {
  emit('change', count)
}

function onReset() {
  emit('reset')
}
</script>

<template>
  {{ count }}

  <button @click="onChange">Change</button>
  <button @click="emit('change', count)">Change (direct)</button>
  <button @click="onReset">Reset</button>
  <button @click="emit('reset')">Reset (direct)</button>
</template>

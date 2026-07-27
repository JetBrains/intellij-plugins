<script setup lang="ts">
const emit = defineEmits<{
  (e: 'change', id: number): void
  (e: 'update', value: string): void
}>()

function onChange() {
  emit('change', 1)
}
</script>

<template>
  <button @click="onChange">Change</button>
  <button @click="emit('change', 1)">Change (direct)</button>
  <button @click="emit('update', 'value')">Update</button>
</template>

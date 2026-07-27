<script setup lang="ts">
const emit = defineEmits<{
  change: [id: number, source: string]
}>()

function onChange() {
  emit('change', 1, 'user')
}
</script>

<template>
  <button @click="onChange">Change</button>
  <button @click="emit('change', 1, 'user')">Change (direct)</button>
</template>

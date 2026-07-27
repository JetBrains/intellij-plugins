<script setup lang="ts">
const { count = 1 } = defineProps<{ count?: number }>()

const emit = defineEmits<{
  change: [id: number]
}>()

function onChange() {
  emit('change', count)
}
</script>

<template>
  {{ count }}
  <button @click="onChange">Change</button>
  <button @click="emit('change', count)">Change (direct)</button>
</template>

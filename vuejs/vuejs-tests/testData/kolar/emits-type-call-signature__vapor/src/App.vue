<script vapor lang="ts">
defineEmits<{
  (e: 'change', id: number): void
  (e: 'update', value: string): void
}>()
</script>

<template>
  <button @click="$emit('change', 1)">Change</button>
  <button @click="$emit('update', 'value')">Update</button>
</template>

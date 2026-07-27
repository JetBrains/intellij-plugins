<script setup lang="ts">
const { count } = defineProps<{ count: number }>()
</script>

<template>
  {{ count }}
  <div :title="count">
    <span>{{ count }}</span>
  </div>
</template>

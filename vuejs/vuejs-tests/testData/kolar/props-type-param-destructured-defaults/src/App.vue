<script setup lang="ts">
const { count = 1 } = defineProps<{ count?: number }>()
</script>

<template>
  {{ count }}
  <div :title="count">
    <span>{{ count }}</span>
  </div>
</template>

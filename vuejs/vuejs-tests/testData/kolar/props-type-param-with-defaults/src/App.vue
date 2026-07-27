<script setup lang="ts">
const props = withDefaults(defineProps<{ count?: number }>(), { count: 1 })
</script>

<template>
  {{ props.count }}
  <div :title="props.count">
    <span>{{ props.count }}</span>
  </div>
</template>

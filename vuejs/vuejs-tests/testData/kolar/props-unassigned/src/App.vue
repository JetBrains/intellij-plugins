<script setup lang="ts">
defineProps<{ count: number }>()
</script>

<template>
  {{ $props.count }}
  <div :title="$props.count">
    <span>{{ $props.count }}</span>
  </div>
</template>

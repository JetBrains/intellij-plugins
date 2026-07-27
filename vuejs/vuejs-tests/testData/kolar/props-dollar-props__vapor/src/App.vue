<script vapor lang="ts">
const props = defineProps<{ count: number }>()
</script>

<template>
  {{ props.count }}
  {{ $props.count }}
  <div :title="props.count">
    <span>{{ props.count }}</span>
    <span :data-count="$props.count">{{ $props.count }}</span>
  </div>
</template>

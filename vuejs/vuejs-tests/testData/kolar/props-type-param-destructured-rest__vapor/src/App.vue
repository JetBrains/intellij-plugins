<script vapor lang="ts">
const { count, ...rest } = defineProps<{ count: number, label: string }>()
</script>

<template>
  {{ count }}
  {{ rest.label }}
  <div :title="count">
    <span>{{ count }}</span>
    <span :data-label="rest.label">{{ rest.label }}</span>
  </div>
</template>

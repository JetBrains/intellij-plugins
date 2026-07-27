<script vapor lang="ts">
const props = defineProps({
  count: Number,
  label: { type: String, required: true, default: '' },
})
</script>

<template>
  {{ props.count }}
  {{ props.label }}
  <div :title="props.label">
    <span>{{ props.count }}</span>
    <span>{{ props.label }}</span>
  </div>
</template>

<script vapor lang="ts">
import type { PropType } from 'vue'

const props = defineProps({
  shape: { type: Object as PropType<{ x: number, y: number }>, default: () => ({ x: 0, y: 0 }) },
})
</script>

<template>
  {{ props.shape.x }}
  {{ props.shape.y }}
  <div :title="props.shape.x">
    <span>{{ props.shape.x }}</span>
    <span>{{ props.shape.y }}</span>
  </div>
</template>

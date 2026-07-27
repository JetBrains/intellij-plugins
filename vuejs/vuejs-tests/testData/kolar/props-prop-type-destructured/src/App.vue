<script setup lang="ts">
import type { PropType } from 'vue'

const { shape } = defineProps({
  shape: { type: Object as PropType<{ x: number, y: number }> },
})
</script>

<template>
  {{ shape.x }}
  {{ shape.y }}
  <div :title="shape.x">
    <span>{{ shape.x }}</span>
    <span>{{ shape.y }}</span>
  </div>
</template>

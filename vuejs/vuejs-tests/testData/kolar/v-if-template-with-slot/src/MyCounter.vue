<script setup lang="ts">
defineSlots<{
  default(props: { count: number }): any
}>()
</script>

<template>
  <slot :count="1" />
</template>

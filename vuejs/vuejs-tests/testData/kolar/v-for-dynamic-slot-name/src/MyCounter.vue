<script setup lang="ts">
defineSlots<{
  header(props: { value: number }): any
  footer(props: { value: number }): any
}>()
</script>

<template>
  <slot name="header" :value="1" />
  <slot name="footer" :value="2" />
</template>

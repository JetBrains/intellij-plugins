<script setup lang="ts">
const count = defineModel<number>('count')
</script>

<template>
  <div>{{ count }}</div>
</template>

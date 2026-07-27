<script setup lang="ts">
const { count } = defineProps(['count'])
</script>

<template>
  {{ count }}
  <div :title="count">
    <span>{{ count }}</span>
  </div>
</template>

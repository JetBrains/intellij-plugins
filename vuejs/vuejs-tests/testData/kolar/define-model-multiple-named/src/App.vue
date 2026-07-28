<script setup lang="ts">
const title = defineModel<string>('title')
const count = defineModel<number>('count')
</script>

<template>
  {{ title }} - {{ count }}
</template>

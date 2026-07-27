<script setup lang="ts">
const source: number[] | Record<string, number> = [1, 2, 3]
</script>

<template>
  <ul>
    <li v-for="(value, key, index) in source" :key="index">{{ value }}</li>
  </ul>
</template>

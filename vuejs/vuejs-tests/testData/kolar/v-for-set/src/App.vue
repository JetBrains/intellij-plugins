<script setup lang="ts">
const tags = new Set(['a', 'b', 'c'])
</script>

<template>
  <ul>
    <li v-for="tag in tags" :key="tag">{{ tag }}</li>
  </ul>
</template>

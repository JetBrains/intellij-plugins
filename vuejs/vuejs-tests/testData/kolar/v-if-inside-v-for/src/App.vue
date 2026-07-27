<script setup lang="ts">
const items = [1, 2, 3]
</script>

<template>
  <ul>
    <template v-for="item in items" :key="item">
      <li v-if="item > 1">{{ item }}</li>
    </template>
  </ul>
</template>

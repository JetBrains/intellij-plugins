<script setup lang="ts">
const items = ['a', 'b', 'c']
</script>

<template>
  <ul>
    <li v-for="item in items" :key="item" v-once>{{ item }}</li>
  </ul>
</template>

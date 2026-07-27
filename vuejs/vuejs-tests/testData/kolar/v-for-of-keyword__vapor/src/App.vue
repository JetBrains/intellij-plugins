<script vapor lang="ts">
const items = ['a', 'b', 'c']
</script>

<template>
  <ul>
    <li v-for="item of items" :key="item">{{ item }}</li>
  </ul>
</template>

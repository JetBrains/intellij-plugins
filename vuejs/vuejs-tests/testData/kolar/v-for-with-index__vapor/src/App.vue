<script vapor lang="ts">
const items = ['a', 'b', 'c']
</script>

<template>
  <ul>
    <li v-for="(item, index) in items" :key="index">{{ index }}: {{ item }}</li>
  </ul>
</template>

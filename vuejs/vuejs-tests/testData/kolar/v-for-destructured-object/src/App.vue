<script setup lang="ts">
const items = [{ id: 1, name: 'Alice' }, { id: 2, name: 'Bob' }]
</script>

<template>
  <ul>
    <li v-for="({ id, name }) in items" :key="id">{{ name }}</li>
  </ul>
</template>

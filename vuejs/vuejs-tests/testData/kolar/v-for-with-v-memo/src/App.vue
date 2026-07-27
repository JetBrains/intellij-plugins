<script setup lang="ts">
const items = [{ id: 1, name: 'Alice' }, { id: 2, name: 'Bob' }]
</script>

<template>
  <ul>
    <li v-for="item in items" :key="item.id" v-memo="[item.id]">{{ item.name }}</li>
  </ul>
</template>

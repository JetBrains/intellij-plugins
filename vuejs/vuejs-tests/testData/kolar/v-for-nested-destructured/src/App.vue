<script setup lang="ts">
const items = [{ id: 1, profile: { name: 'Alice' } }, { id: 2, profile: { name: 'Bob' } }]
</script>

<template>
  <ul>
    <li v-for="({ profile: { name } }, index) in items" :key="index">{{ name }}</li>
  </ul>
</template>

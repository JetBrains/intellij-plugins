<script setup lang="ts">
const pairs: [string, number][] = [['a', 1], ['b', 2]]
</script>

<template>
  <ul>
    <li v-for="([label, value]) in pairs" :key="label">{{ label }}: {{ value }}</li>
  </ul>
</template>

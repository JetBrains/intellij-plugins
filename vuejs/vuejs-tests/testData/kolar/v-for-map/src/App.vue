<script setup lang="ts">
const scores = new Map<string, number>([['alice', 1], ['bob', 2]])
</script>

<template>
  <ul>
    <li v-for="(entry, index) in scores" :key="index">{{ entry }}</li>
  </ul>
</template>

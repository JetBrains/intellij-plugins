<script setup lang="ts">
const word = 'abc'
</script>

<template>
  <ul>
    <li v-for="(char, index) in word" :key="index">{{ char }}</li>
  </ul>
</template>

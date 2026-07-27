<script setup lang="ts">
let a = 1
let b = 2
</script>

<template>
  <div v-if="(a++, b > 1)">{{ b }}</div>
</template>

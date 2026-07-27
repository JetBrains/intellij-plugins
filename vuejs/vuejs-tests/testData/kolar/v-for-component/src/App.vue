<script setup lang="ts">
import MyCounter from './MyCounter.vue'

const items = [1, 2, 3]
</script>

<template>
  <MyCounter v-for="item in items" :key="item" :count="item" />
</template>

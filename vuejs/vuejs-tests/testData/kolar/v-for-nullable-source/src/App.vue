<script setup lang="ts">
import { ref } from 'vue'

const items = ref<string[] | null>(null)
</script>

<template>
  <ul>
    <li v-for="item in items" :key="item">{{ item }}</li>
  </ul>
</template>

<script vapor lang="ts">
import { ref } from 'vue'

const items = [1, 2, 3]
const itemRefs = ref<HTMLDivElement[]>()
</script>

<template>
  <div v-for="item in items" :key="item" ref="itemRefs">{{ item }}</div>
</template>

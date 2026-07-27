<script setup lang="ts">
import { ref } from 'vue'

const show = ref(true)
const items = [1, 2, 3]
</script>

<template>
  <button @click="show = !show">Toggle</button>
  <div v-if="show" v-memo="[items.length]">{{ items.length }}</div>
</template>

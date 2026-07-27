<script setup lang="ts">
import { ref } from 'vue'

const show = ref(true)
</script>

<template>
  <button @click="show = !show">Toggle</button>
  <template v-if="show">
    <div>First</div>
    <div>Second</div>
  </template>
  <template v-else>
    <span>Fallback</span>
  </template>
</template>

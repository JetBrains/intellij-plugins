<script setup lang="ts">
import { ref } from 'vue'

const show = ref(true)
</script>

<template>
  <button @click="show = !show">Toggle</button>
  <template v-if="show" :key="'a'">
    <div>On</div>
  </template>
  <template v-else :key="'b'">
    <div>Off</div>
  </template>
</template>

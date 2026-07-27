<script setup lang="ts">
import { ref } from 'vue'

const show = ref(true)
</script>

<template>
  <button @click="show = !show">Toggle</button>
  <div v-if="show">Visible</div>
</template>

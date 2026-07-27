<script vapor lang="ts">
import { ref } from 'vue'
import MyCounter from './MyCounter.vue'

const show = ref(true)
</script>

<template>
  <button @click="show = !show">Toggle</button>
  <MyCounter v-if="show">
    <span>Implicit default slot content</span>
  </MyCounter>
</template>

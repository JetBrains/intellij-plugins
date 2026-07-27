<script setup lang="ts">
import { ref } from 'vue'
import MyCounter from './MyCounter.vue'

const show = ref(true)
</script>

<template>
  <button @click="show = !show">Toggle</button>
  <MyCounter>
    <template v-if="show" #default="{ count }">{{ count }}</template>
    <template v-else #default>Fallback</template>
  </MyCounter>
</template>

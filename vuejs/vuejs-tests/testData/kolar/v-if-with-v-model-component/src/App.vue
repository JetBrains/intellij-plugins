<script setup lang="ts">
import { ref } from 'vue'
import MyCounter from './MyCounter.vue'

const show = ref(true)
const count = ref(0)
</script>

<template>
  <button @click="show = !show">Toggle</button>
  <MyCounter v-if="show" v-model:count="count" />
</template>

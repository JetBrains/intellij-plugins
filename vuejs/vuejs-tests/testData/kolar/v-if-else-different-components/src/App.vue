<script setup lang="ts">
import { ref } from 'vue'
import MyCounter from './MyCounter.vue'
import MyLabel from './MyLabel.vue'

const showCounter = ref(true)
</script>

<template>
  <button @click="showCounter = !showCounter">Toggle</button>
  <MyCounter v-if="showCounter" />
  <MyLabel v-else />
</template>

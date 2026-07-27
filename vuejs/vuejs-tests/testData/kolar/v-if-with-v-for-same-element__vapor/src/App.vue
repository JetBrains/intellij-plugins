<script vapor lang="ts">
import { ref } from 'vue'

const items = ['a', 'b', 'c']
const showList = ref(true)
</script>

<template>
  <button @click="showList = !showList">Toggle</button>
  <ul>
    <li v-if="showList" v-for="item in items" :key="item">{{ item }}</li>
  </ul>
</template>

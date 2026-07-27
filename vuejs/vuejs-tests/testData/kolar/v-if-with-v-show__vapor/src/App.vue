<script vapor lang="ts">
import { ref } from 'vue'

const showIf = ref(true)
const showShow = ref(true)
</script>

<template>
  <button @click="showIf = !showIf">Toggle if</button>
  <button @click="showShow = !showShow">Toggle show</button>
  <div v-if="showIf" v-show="showShow">Content</div>
</template>

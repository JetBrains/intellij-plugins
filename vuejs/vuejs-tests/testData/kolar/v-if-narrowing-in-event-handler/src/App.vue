<script setup lang="ts">
interface Item {
  bar?: {
    baz: () => void
  }
}

const example: Item = {}
</script>

<template>
  <button v-if="example.bar" @click="example.bar.baz()">Run</button>
</template>

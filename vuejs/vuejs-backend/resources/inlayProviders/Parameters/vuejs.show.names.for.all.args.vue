<script setup lang="ts">
function example(param: number[]) {}
let data = [1, 2, 3]
</script>

<template>
  <div v-bind:title="example(data)"></div>
</template>

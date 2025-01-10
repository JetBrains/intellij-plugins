<script setup lang="ts">
function example(param: number[]) {}
</script>

<template>
  <div v-bind:title="example([1, 2, 3])"></div>
</template>

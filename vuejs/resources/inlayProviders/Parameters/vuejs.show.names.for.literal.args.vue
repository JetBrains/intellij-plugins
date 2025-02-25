<script setup lang="ts">
function example(param: number) {}
</script>

<template>
  <div v-bind:title="example(123)"></div>
</template>

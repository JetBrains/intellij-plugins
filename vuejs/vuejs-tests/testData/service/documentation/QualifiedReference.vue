<script setup lang="ts">
let <error>dummyErrorToSatisfyTestFramework</error>: string = 1;

let local = 323;
</script>

<template>
  <div v-text="local.<caret>toFixed()"></div>
</template>
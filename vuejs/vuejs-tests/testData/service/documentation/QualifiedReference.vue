<script setup lang="ts">

let local = 323;
</script>

<template>
  <div v-text="local.<caret>toFixed()"></div>
</template>
<script setup lang="ts">
const props = withDefaults(defineProps<{
  msg?: string;
}>(), {
  msg: 'This is the default'
})
</script>

<template>
  <h1>{{ msg }}</h1>
</template>
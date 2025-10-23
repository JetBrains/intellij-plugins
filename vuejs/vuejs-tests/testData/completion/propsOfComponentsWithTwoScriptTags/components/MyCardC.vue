<script setup lang="ts">
defineProps<{
  name: string,
  age: number,
  color: string,
  address: {
    street: string,
    city: string,
    house: number,
  },
}>()
</script>

<script lang="ts">
export type Test = string;
</script>

<template>
  <h1>Card C: {{ msg }}</h1>
</template>

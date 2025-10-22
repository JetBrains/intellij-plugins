<script vapor lang="ts">
defineProps<{ msg: string }>()
</script>

<script lang="ts">
export type Test = string;
</script>

<template>
  <h1>Card D: {{ msg }}</h1>
</template>

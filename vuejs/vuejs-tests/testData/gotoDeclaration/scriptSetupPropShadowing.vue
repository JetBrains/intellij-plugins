<script setup lang="ts">
const foo = 2; // source order is irrelevant
defineProps<{ foo: string }>()
</script>

<template>
  {{foo}}
</template>
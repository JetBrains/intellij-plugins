<script setup lang="ts">
const props = defineProps<{
  newName: string
}>()
</script>

<template>
  {{ newName }}
  {{ props.newName }}
</template>

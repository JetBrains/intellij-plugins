<script setup lang="ts">
const props = defineProps({
  newName: {
    type: String,
    required: true
  }
})
</script>

<template>
  {{ newName }}
</template>

<script setup lang="ts">
const props = defineProps({
  optionsApiWithType: {
    type: String,
    required: true
  }
})
</script>

<template>
  {{ optionsApiWithType }}
</template>

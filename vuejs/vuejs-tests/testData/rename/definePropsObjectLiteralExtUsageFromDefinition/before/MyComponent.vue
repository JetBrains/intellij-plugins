<script setup lang="ts">
const props = defineProps({
  opt<caret>ionsApiWithType: {
    type: String,
    required: true
  }
})
</script>

<template>
  {{ optionsApiWithType }}
</template>

<script setup lang="ts">
const props = defineProps<{
  optionsApiWithType: string
}>()
</script>

<template>
  {{opt<caret>ionsApiWithType}}
  {{props.optionsApiWithType}}
</template>

<script setup lang="ts">
const props = defineProps<{
  optionsApiWithType: string
}>()
</script>

<template>
  {{optionsApiWithType}}
  {{props.optionsApiWithType}}
</template>

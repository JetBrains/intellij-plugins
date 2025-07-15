<script setup lang="ts">
export interface MyProps {
  optionsApiWithType: string
}
const props = defineProps<MyProps>()
</script>

<template>
  {{ optionsApiWithType }}
  {{ props.optionsApiWithType }}
</template>

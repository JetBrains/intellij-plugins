<script setup lang="ts">
export interface MyProps {
  opt<caret>ionsApiWithType: string
}
const props = defineProps<MyProps>()
</script>

<template>
  {{ optionsApiWithType }}
  {{ props.optionsApiWithType }}
</template>

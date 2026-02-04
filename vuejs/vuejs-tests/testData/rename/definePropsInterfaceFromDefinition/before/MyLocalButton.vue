<script setup lang="ts">
interface MyProps {
  opt<caret>ionsApiWithType: string
}
const props = defineProps<MyProps>()
</script>

<template>
  {{optionsApiWithType}}
  {{props.optionsApiWithType}}
</template>

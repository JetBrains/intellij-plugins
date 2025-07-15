<script setup lang="ts">
export interface MyProps {
  newName: string
}
const props = defineProps<MyProps>()
</script>

<template>
  {{ newName }}
  {{ props.newName }}
</template>

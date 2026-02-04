<script setup lang="ts">
interface MyProps {
  newName: string
}
const props = defineProps<MyProps>()
</script>

<template>
  {{ newName }}
  {{ props.newName }}
</template>

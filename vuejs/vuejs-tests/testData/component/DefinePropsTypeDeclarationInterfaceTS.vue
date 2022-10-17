<script setup lang="ts">
// TODO Props should not be recognized as a component
interface Props {
  optional?: string,
  required: string,
}

defineProps<Props>();
</script>

<template>
  {{optional}}
  {{required}}
</template>

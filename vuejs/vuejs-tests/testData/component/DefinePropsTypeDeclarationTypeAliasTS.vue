<script setup lang="ts">
type Props = {
  optional?: string,
  required: string,
}

defineProps<Props>();
</script>

<template>
  {{optional}}
  {{required}}
</template>

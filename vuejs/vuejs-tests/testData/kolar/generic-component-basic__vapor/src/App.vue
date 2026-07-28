<script vapor lang="ts" generic="T">
defineProps<{ item: T }>()
</script>

<template>
  {{ $props.item }}
</template>

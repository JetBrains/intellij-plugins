<script vapor lang="ts" generic="T">
defineProps<{ item: T }>()
defineEmits<{ select: [item: T] }>()
</script>

<template>
  <button @click="$emit('select', $props.item)">{{ $props.item }}</button>
</template>

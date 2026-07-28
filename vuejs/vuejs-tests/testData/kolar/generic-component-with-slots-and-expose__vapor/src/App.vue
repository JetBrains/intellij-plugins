<script vapor lang="ts" generic="T">
defineProps<{ item: T }>()
defineSlots<{
  default(props: { item: T }): any
}>()

function reset(): void {}

defineExpose<{ reset(): void }>()
</script>

<template>
  <slot :item="$props.item" />
</template>

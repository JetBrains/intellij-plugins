<script vapor lang="ts">
defineSlots<{
  default(props: { msg: string }): any
}>()
</script>

<template>
  <slot :msg="'hello'" />
</template>

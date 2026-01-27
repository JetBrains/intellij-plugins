<script setup lang="ts">
defineSlots<{
  default: (props: { msg: string }) => any
  header?: (props: { pageTitle?: string }) => any
  footer: (props: { year?: number }) => any
}>()
</script>

<template>
  <div class="container">
    <header>
      <slot name="<caret>"></slot>
    </header>
  </div>
</template>
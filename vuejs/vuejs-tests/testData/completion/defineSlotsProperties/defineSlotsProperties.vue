<script setup lang="ts">
defineSlots<{
  default: (props: { msg: string }) => any
  header?: (props: { pageTitle?: string }) => any
  footer: (props: { year?: number, age: number, companyName: string }) => any
}>()
</script>

<template>
  <div class="container">
    <header>
      <slot name="footer" <caret>></slot>
    </header>
  </div>
</template>
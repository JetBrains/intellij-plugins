<script setup lang="ts">
  defineProps<{
    onfoo: string,
    on: boolean,
    onChange: (value: string) => void
  }>()
</script>

<template>
  <no-filter-for-on-props :o<caret>/>
</template>
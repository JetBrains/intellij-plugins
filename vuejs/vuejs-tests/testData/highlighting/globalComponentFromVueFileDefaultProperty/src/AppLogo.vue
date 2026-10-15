<script setup lang="ts">
defineProps<{
  logoSize: number
  logoTitle?: string
}>()
</script>

<template>
  <img src="/favicon.ico" :alt="logoTitle" :width="logoSize"/>
</template>

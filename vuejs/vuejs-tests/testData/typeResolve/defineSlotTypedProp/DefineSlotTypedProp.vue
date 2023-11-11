<script setup lang="ts">
import SlotsProvider from "./SlotsProvider.vue";
</script>

<template>
  <slots-provider>
    <template #default="{bar}">{{ bar }}</template>
    <template #header="{company}">{{ company }}</template>
    <template #item="{data}">{{ data }}</template>
  </slots-provider>
</template>

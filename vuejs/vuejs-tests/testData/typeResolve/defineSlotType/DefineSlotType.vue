<script setup lang="ts">
import SlotsProvider from "./SlotsProvider.vue";
</script>

<template>
  <SlotsProvider>
    <template #header="{pageTitle}">
      <div>{{ pageTitle }}</div>
    </template>
    <template #default="{msg}">
      <div>{{ msg }}</div>
    </template>
    <template #footer="footerProps">
      <div>{{ footerProps.year }}</div>
    </template>
  </SlotsProvider>
</template>
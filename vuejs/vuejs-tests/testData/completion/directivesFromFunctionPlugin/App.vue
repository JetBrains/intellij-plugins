<script setup lang="ts">
</script>

<template>
  <MyButtonFromPlugin v-my-mutate="console.warn"></MyButtonFromPlugin>
  <MyLabelFromPlugin v-my-intersect.pass="console.warn"></MyLabelFromPlugin>
</template>

<script vapor lang="ts">
import MyCounter from './MyCounter.vue'

const names = ['header', 'footer'] as const
</script>

<template>
  <MyCounter>
    <template v-for="name in names" :key="name" v-slot:[name]="slotProps">{{ name }}: {{ slotProps.value }}</template>
  </MyCounter>
</template>

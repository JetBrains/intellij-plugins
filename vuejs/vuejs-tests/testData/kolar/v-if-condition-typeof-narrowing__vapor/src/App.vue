<script vapor lang="ts">
const value: string | number = 'hello'
</script>

<template>
  <span v-if="typeof value === 'string'">{{ value.toUpperCase() }}</span>
  <span v-else>{{ value.toFixed(2) }}</span>
</template>

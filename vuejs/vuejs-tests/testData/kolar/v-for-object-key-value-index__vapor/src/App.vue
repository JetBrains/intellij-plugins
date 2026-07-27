<script vapor lang="ts">
const scores: Record<string, number> = { alice: 1, bob: 2 }
</script>

<template>
  <ul>
    <li v-for="(value, key, index) in scores" :key="key">{{ index }}: {{ key }} = {{ value }}</li>
  </ul>
</template>

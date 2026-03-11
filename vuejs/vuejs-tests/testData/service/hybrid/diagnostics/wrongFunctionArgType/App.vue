<script setup lang="ts">
function greet(name: string): string {
  return 'Hello, ' + name
}
const result: string = greet(123)
</script>
<template>
  <div>{{ result }}</div>
</template>

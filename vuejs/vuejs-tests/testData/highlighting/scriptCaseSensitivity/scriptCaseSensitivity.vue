<script lang="ts" setup>
interface Example {
  uniqueField: number;
}

const example: Example = {
  uniqueField: 0,
}
</script>

<template>
  <div>{{ example.uniqueField }}</div>
  <div>{{ example.name }}</div>
  <div>{{ example.invalidUniqueName }}</div>
</template>

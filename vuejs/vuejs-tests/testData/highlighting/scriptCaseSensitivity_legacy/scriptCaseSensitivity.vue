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
  <div>{{ example.<error descr="Unresolved variable name">name</error> }}</div>
  <div>{{ example.<error descr="Unresolved variable invalidUniqueName">invalidUniqueName</error> }}</div>
</template>

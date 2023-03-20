<script lang="ts" setup>
interface Example {
  bool: boolean;
}

interface RealType {
  uniqueField: number;
}

const example: RealType = {
  uniqueField: 0,
}
</script>

<template>
  <div>{{ example }}</div>
</template>

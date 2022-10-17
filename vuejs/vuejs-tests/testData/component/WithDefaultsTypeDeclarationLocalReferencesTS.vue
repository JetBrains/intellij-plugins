<script setup lang="ts">
const defaults = {
  prop: 1,
};
withDefaults(defineProps<{
  prop: number | undefined,
}>(), {...defaults}); // not supported by Vue, ignore

prop; // TODO Should be unresolved
</script>

<template>
  {{prop}}
</template>

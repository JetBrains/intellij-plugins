<template>
  <div>
    <enum :bar="<caret>"></enum>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  bar?: 'a' | 'b' | 'c';
}>();
</script>
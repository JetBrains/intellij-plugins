<script setup lang="ts">
defineProps({
  optional: String,
  optionalUnion: [String, Number],
  required: { type: String, required: true },
  requiredUnion: { type: [String, Number], required: true },
});

<error descr="Unresolved variable or type optional">optional</error>;
</script>

<template>
  {{optional}}
  {{optionalUnion}}
  {{required}}
  {{requiredUnion}}
</template>

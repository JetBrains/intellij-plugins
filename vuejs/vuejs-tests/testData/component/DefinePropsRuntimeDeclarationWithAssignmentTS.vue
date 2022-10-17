<script setup lang="ts">
const props = defineProps({
  optional: String,
  optionalUnion: [String, Number],
  required: { type: String, required: true },
  requiredUnion: { type: [String, Number], required: true },
});

props.optional;
<error descr="Unresolved variable or type optional">optional</error>;
</script>

<template>
  {{optional}}
  {{optionalUnion}}
  {{required}}
  {{requiredUnion}}
  {{props.optional}}
</template>

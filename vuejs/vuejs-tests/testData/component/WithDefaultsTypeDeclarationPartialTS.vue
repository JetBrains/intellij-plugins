<!--
Expected Component Model:
Prop(name=stillProp, required=true, presentableType=number)
-->
<script setup lang="ts">
<error descr="Invalid number of arguments, expected 2">withDefaults</error>(defineProps<{
  stillProp: number,
}>());

stillProp; // TODO Should be unresolved
</script>

<template>
  {{stillProp}}
</template>

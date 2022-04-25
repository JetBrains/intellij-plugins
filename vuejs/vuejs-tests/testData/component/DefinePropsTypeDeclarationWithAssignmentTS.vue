<!--
Expected Component Model:
Prop(name=optional, required=false, presentableType=string | undefined)
Prop(name=required, required=true, presentableType=string)
-->
<script setup lang="ts">
const props = defineProps<{
  optional?: string,
  required: string,
}>();

props.optional;
<error descr="Unresolved variable or type optional">optional</error>;
</script>

<template>
  {{optional}}
  {{required}}
  {{props.optional}}
</template>

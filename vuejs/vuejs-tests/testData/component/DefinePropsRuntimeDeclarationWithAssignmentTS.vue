<!--
Expected Component Model:
Prop(name=optional, required=false, presentableType=string | undefined)
Prop(name=optionalUnion, required=false, presentableType=String | Number | undefined)
Prop(name=required, required=true, presentableType=string)
Prop(name=requiredUnion, required=true, presentableType=String | Number)
-->
<script setup lang="ts">
const props = defineProps({
  optional: String,
  optionalUnion: [String, Number], // TODO buggy, presentableType should be lowercase
  required: { type: String, required: true },
  requiredUnion: { type: [String, Number], required: true }, // TODO buggy, presentableType should be lowercase
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

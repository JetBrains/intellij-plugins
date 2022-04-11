<!--
Expected Component Model:
Prop(name=optional, required=false, presentableType=string | undefined)
Prop(name=optionalUnion, required=false, presentableType=String | Number | undefined)
Prop(name=required, required=true, presentableType=string)
Prop(name=requiredUnion, required=true, presentableType=String | Number)
-->
<script setup lang="ts">
defineProps({
  optional: String,
  optionalUnion: [String, Number], // buggy, presentableType should be lowercase
  required: { type: String, required: true },
  requiredUnion: { type: [String, Number], required: true }, // buggy, presentableType should be lowercase
});
</script>

<template>
  {{optional}}
  {{optionalUnion}}
  {{required}}
  {{requiredUnion}}
</template>

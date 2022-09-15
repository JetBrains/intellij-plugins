<!--
Expected Component Model:
Prop(name=optional, required=false, presentableType=string | undefined)
Prop(name=optionalUnion, required=false, presentableType=string | number | undefined)
Prop(name=required, required=true, presentableType=string)
Prop(name=requiredUnion, required=true, presentableType=string | number)
-->
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

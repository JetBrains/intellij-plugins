<!--
Expected Component Model:
Prop(name=optional, required=false, presentableType=string)
Prop(name=optionalUnion, required=false, presentableType=string | number)
Prop(name=required, required=true, presentableType=string)
Prop(name=requiredUnion, required=true, presentableType=string | number)
-->
<script>
export default {
  name: "TestComponent",
  props: {
    optional: String,
    optionalUnion: [String, Number],
    required: { type: String, required: true },
    requiredUnion: { type: [String, Number], required: true },
  }
}
</script>

<template>
  {{optional}}
  {{optionalUnion}}
  {{required}}
  {{requiredUnion}}
</template>

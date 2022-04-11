<!--
Expected Component Model:
Prop(name=optional, required=false, presentableType=string)
Prop(name=optionalUnion, required=false, presentableType=String | Number)
Prop(name=required, required=true, presentableType=string)
Prop(name=requiredUnion, required=true, presentableType=String | Number)
-->
<script>
export default {
  name: "TestComponent",
  props: {
    optional: String,
    optionalUnion: [String, Number], // buggy, presentableType should be lowercase
    required: { type: String, required: true },
    requiredUnion: { type: [String, Number], required: true }, // buggy, presentableType should be lowercase
  }
}
</script>

<template>
  {{optional}}
  {{optionalUnion}}
  {{required}}
  {{requiredUnion}}
</template>

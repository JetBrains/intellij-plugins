<!--
Expected Component Model:
Prop(name=optional, required=false, presentableType=null)
Prop(name=required, required=false, presentableType=null)
-->
<script>
export default {
  name: "TestComponent",
  props: ["optional", "required"]
}
</script>

<template>
  {{optional}}
  {{required}}
</template>

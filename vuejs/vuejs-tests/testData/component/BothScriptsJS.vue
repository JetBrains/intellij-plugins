<!--
Expected Component Model:
Prop(name=optional, required=false, presentableType=Struct)
-->
<script>
export class Struct {
  a = 1;
  b = "hello";
}

export default {
  name: "Rename",
}
</script>

<script setup>
defineProps({
  optional: Struct,
})
</script>

<template>
  {{optional}}
  <!-- Unresolved warning is the WebStorm bug -->
  <Rename
    :optional="new <weak_warning descr="Unresolved type Struct">Struct</weak_warning>()"
    v-if="optional === undefined"
  />
</template>

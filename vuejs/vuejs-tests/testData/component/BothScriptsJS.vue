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
};
</script>

<script setup>
defineProps({
  optional: Struct,
});

<weak_warning descr="Element is not exported">optional</weak_warning>; // TODO Should be unresolved, "Element is not exported" should be suppressed
</script>

<template>
  {{optional}}
  <!-- Unresolved warning is the WebStorm bug -->
  <Rename
    :optional="new <weak_warning descr="Unresolved type Struct">Struct</weak_warning>()"
    v-if="optional === undefined"
  />
</template>

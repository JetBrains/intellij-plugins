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

<weak_warning descr="Unresolved variable or type optional">optional</weak_warning>;
</script>

<template>
  {{optional}}
  <Rename
    :optional="new Struct()"
    v-if="optional === undefined"
  />
  {{new Struct()}}
</template>

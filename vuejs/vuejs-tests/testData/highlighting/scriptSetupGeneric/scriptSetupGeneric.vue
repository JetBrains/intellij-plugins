<script lang="ts">
import * as Vue from "vue" // load defineProps
interface TopObj {
  b: string
}
let foo: Clearable
foo.a;
foo.b;
</script>

<script
  setup
  lang="ts"
  generic="Clearable extends TopObj, ValueType extends MyObj"
>
interface MyObj {
  a: string
}

const {clearable, value} = defineProps<{
  clearable: Clearable;
  value: ValueType;
}>()

clearable.a;
clearable.b;
value.a;
value.b;

</script>
<template>
  <div v-if="(value as Clearable).a"></div>
  <div v-if="(value as Clearable).b"></div>
  {{ (value as Clearable).a }}
  {{ (value as Clearable).b }}
  <div v-if="value.a"></div>
  <div v-if="value.b"></div>
  {{ value.a }}
  {{ value.b }}
  <div v-if="(clearable as ValueType).a"></div>
  <div v-if="(clearable as ValueType).b"></div>
  {{ (clearable as ValueType).a }}
  {{ (clearable as ValueType).b }}
  <div v-if="clearable.a"></div>
  <div v-if="clearable.b"></div>
  {{ clearable.a }}
  {{ clearable.b }}
</template>
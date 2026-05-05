<script lang="ts">
import * as Vue from "vue" // load defineProps
interface TopObj {
  b: string
}
let foo: <error descr="Unresolved type Clearable">Clearable</error>
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

clearable.<error descr="Unresolved variable a">a</error>;
clearable.b;
value.a;
value.<error descr="Unresolved variable b">b</error>;

</script>
<template>
  <div v-if="(value as Clearable).<error descr="Unresolved variable a">a</error>"></div>
  <div v-if="(value as Clearable).b"></div>
  {{ (value as Clearable).<error descr="Unresolved variable a">a</error> }}
  {{ (value as Clearable).b }}
  <div v-if="value.a"></div>
  <div v-if="value.<error descr="Unresolved variable b">b</error>"></div>
  {{ value.a }}
  {{ value.<error descr="Unresolved variable b">b</error> }}
  <div v-if="(clearable as ValueType).a"></div>
  <div v-if="(clearable as ValueType).<error descr="Unresolved variable b">b</error>"></div>
  {{ (clearable as ValueType).a }}
  {{ (clearable as ValueType).<error descr="Unresolved variable b">b</error> }}
  <div v-if="clearable.<error descr="Unresolved variable a">a</error>"></div>
  <div v-if="clearable.b"></div>
  {{ clearable.<error descr="Unresolved variable a">a</error> }}
  {{ clearable.b }}
</template>
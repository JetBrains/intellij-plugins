<script setup lang="ts">
<error descr="TS2304: Cannot find name 'unresolved1'.">unresolved1</error>
// todo fix error below, probably a problem with test dependencies
<error descr="TS2304: Cannot find name 'defineProps'.">defineProps</error><{foo: string}>()
const expectString = (x: string): string => x;
</script>

<template>
  <div>{{<error descr="Unresolved variable or type unresolved2">unresolved2</error>}}</div>
  <div v-text="<error descr="Unresolved variable or type unresolved3">unresolved3</error>" />
  <TSErrorsInsideTemplate :foo="<error descr="Type true is not assignable to type string">true</error>" />
  {{expectString(<error descr="Argument type true is not assignable to parameter type string">true</error>)}}
</template>

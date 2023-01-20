<script setup lang='ts'>
<error descr="TS2304: Cannot find name 'unresolved1'.">unresolved1</error>
</script>

<template>
  <div>{{<error descr="Unresolved variable or type unresolved2">unresolved2</error>}}</div>
  <div v-text="<error descr="Unresolved variable or type unresolved3">unresolved3</error>" />
</template>
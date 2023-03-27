<script setup lang='ts'>
const bar: string | number = 'hello';

function isString(x: unknown): x is string {
  return typeof x === "string"
}
</script>

<template>
  <div v-if='isString(bar) && true'>
    <div v-text='bar.length'></div>
    <div v-text='bar.<error descr="Unresolved variable unresolved">unresolved</error>'></div>
    <div v-text='true || false'></div>
    <div v-text="bar.length !== 5"></div>
  </div>
  <div v-text='bar.<error descr="Unresolved variable length">length</error>'></div>
  <div v-text='isString(bar) && bar.length'></div>
  <div v-text='isString(bar) ? bar.length : bar.<error descr="Unresolved variable length">length</error>'></div>
  <div v-if='isString(bar) && bar.length'>
    <div v-text="bar.length !== 5"></div>
  </div>
</template>

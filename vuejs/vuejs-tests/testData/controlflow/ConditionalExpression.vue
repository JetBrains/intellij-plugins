<script setup lang='ts'>
const bar: string | number = 'hello';

function isString(x: unknown): x is string {
  return typeof x === "string"
}
</script>

<template>
  <div v-text='isString(bar) ? bar.length : bar.<error descr="Unresolved variable length">length</error>'></div>
</template>
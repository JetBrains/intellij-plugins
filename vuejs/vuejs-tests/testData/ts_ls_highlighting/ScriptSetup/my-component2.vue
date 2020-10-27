<template>
  <button @click='inc'>{{ count }}</button>
  <button @click='dec'>{{ count }}</button>
  <button>{{<weak_warning descr="Unresolved variable or type foo">foo</weak_warning>}}</button>
  <button>{{<weak_warning descr="Unresolved variable or type bar">bar</weak_warning>}}</button>
</template>

<script lang='ts'>
import {ref} from "vue";

export const count = ref(0);
export const inc = () => count.value++;

const bar = 12;

export function dec() {
  return count.value--;
}
</script>
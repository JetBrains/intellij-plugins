<template>
  <button @click='inc'>{{ count }}</button>
  <button @click='dec'>{{ count }}</button>
  <button>{{<error descr="Unresolved variable or type foo">foo</error>}}</button>
  <button>{{bar}}</button>
  <foo-bar></foo-bar>
  <FooBard></FooBard>
  <div v-click-outside></div>
</template>

<script setup lang='ts'>
import {ref} from "vue";
import FooBar from "fooBar.vue"
import FooBard from "fooBar.vue"
<warning descr="Unused  import FooBars from \"fooBar.vue\" ">import FooBars from "fooBar.vue"</warning>
import {directive as vClickOutside } from 'v-click-outside'
<warning descr="Unused import {directive as vClickOutsides } from 'v-click-outside'">import {directive as vClickOutsides } from 'v-click-outside'</warning>

export const count = ref(0);
export const inc = () => count.value++;

const bar = 12;

export function dec() {
  return count.value--;
}
</script>
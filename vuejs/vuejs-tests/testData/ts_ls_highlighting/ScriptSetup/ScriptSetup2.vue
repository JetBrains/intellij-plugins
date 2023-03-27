<script lang="ts">
import {defineComponent} from "vue"
const <error descr="TS6133: 'foo' is declared but its value is never read.">foo</error> = 123
export default defineComponent({
  props: {
    a: Boolean
  }
})
<error descr="TS2304: Cannot find name 'bar'.">bar</error> = 234;
const <error>await</error> = "expect error";
</script>

<script setup lang='ts'>
import {ref} from "vue"
import MyComponent from "./my-component.vue"
import MyComponent2 from "./my-component2.vue"


let bar = 123;

<error descr="Attempt to assign to const or readonly variable"><error descr="TS2588: Cannot assign to 'foo' because it is a constant.">foo</error></error> = 12
const <error>await</error> = "expect error";
</script>

<template>
  <MyComponent :count="12" :bar="'foo'"/>
  <<warning descr="Unknown html tag MyComponentTypo">MyComponentTypo</warning>/>
  <MyComponent2/>
  {{ bar }}
  {{ foo }}
  {{ <error descr="Unresolved variable or type undeclared">undeclared</error> }}
</template>

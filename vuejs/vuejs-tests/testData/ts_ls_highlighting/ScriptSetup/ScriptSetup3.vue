<script lang='ts'>
import {defineComponent} from "vue"
const <error descr="TS6133: 'foo' is declared but its value is never read.">foo</error> = 123
export default defineComponent({
  props: {
    a: Boolean
  }
})
<error descr="TS2304: Cannot find name 'bar'.">bar</error> = 234;
</script>

<script setup>
import {ref} from "vue"
import MyComponent from "./my-component.vue"
import MyComponent2 from "./my-component2.vue"


var bar = 123;

<error descr="Attempt to assign to const or readonly variable">foo</error> = 12

</script>
<template>
  <MyComponent :count="12" :bar="'foo'"/>
  <<warning descr="Unknown html tag MyComponents">MyComponents</warning>/>
  <MyComponent2/>
  {{ bar }}
  {{ foo }}
  {{ <weak_warning descr="Unresolved variable or type foobar">foobar</weak_warning> }}
</template>

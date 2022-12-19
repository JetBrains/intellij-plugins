<script lang='ts'>
import {defineComponent} from "vue";
const <error>foo</error> = 123;
export default defineComponent({
  props: {
    a: Boolean
  }
});
<error>bar</error> = 234;
</script>

<script setup lang='ts'>
import {ref} from "vue"
import MyComponent from "./my-component.vue"
import MyComponent2 from "./my-component2.vue"


var bar = 123;

<error descr="TS2588: Cannot assign to 'foo' because it is a constant.">foo</error> = 12

</script>

<template>
  <MyComponent :count="12" :bar="'foo'"/>
  <<warning descr="Unknown html tag MyComponents">MyComponents</warning>/>
  <MyComponent2/>
  {{ bar }}
  {{ foo }}
  {{ <error descr="Unresolved variable or type foobar">foobar</error> }}
</template>

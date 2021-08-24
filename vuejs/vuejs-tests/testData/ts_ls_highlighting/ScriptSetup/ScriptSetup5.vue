<script setup lang='ts'>
import {ref} from "vue"
import MyComponent from "./my-component.vue"
import <error>MyComponent2</error> from "./my-component2.vue"
import { directive as vClickOutside } from <error>'<error>v-click-outside</error>'</error>

var bar = 123;

<error descr="TS2304: Cannot find name 'foo'.">foo</error> = 12

</script>
<template>
  <MyComponent :count="12" :bar="'foo'"
               v-<warning descr="Unrecognized Vue directive">click-outsde</warning>
               v-click-outside />
  <<warning descr="Unknown html tag MyComponents">MyComponents</warning>/>
  <MyComponent2/>
  {{ bar }}
  {{ foo }}
  {{ <weak_warning descr="Unresolved variable or type foobar">foobar</weak_warning> }}
</template>
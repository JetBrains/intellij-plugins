<template>
  <template v-for="item in items">
    <slot :name="`item.${item.prop}`" v-bind:item="item">
      {{item.value}}
    </slot>
  </template>
  <div v-bind:<warning descr="Unrecognized attribute or property name">foo</warning>="12"/>
  <Bar>
    <template #<warning descr="Unrecognized Slot name">errorLine</warning>></template>
    <template #item.bar></template>
    <template #item.foo="{item}">
      {{ item.value }}
    </template>
  </Bar>
</template>
<script>
import Foo from "./Foo"
export default {
  name:"Bar",
  components: {
    Foo
  },
  data() {
    return {
      items: [
        { prop: "foo", value: 12}
      ]
    }
  }
}
</script>

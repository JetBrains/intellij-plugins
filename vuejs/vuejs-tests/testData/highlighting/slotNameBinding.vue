<template>
  <template v-for="item in items">
    <slot :name="`item.${item.prop}`" v-bind:item="item">
      {{item.value}}
    </slot>
    <slot name="okSlot"/>
  </template>
  <div v-bind:<warning descr="Unrecognized attribute or property name">foo</warning>="12"/>
  <Bar>
    <template #<warning descr="Unrecognized slot name">errorLine</warning>></template>
    <template v-slot:<warning descr="Unrecognized slot name">errorLine</warning>></template>
    <template #item.bar></template>
    <template v-slot:item.bar></template>
    <template #okSlot></template>
    <template v-slot:okSlot></template>
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

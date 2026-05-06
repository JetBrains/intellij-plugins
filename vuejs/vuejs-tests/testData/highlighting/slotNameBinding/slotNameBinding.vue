<template>
  <template v-for="item in items">
    <slot :name="`item.${item.prop}`" v-bind:item="item">
      {{item.value}}
    </slot>
    <slot name="okSlot"/>
  </template>
  <div v-bind:<warning descr="Unrecognized attribute or property name">foo</warning>="12"/>
  <Bar>
    <template #<weak_warning descr="Unrecognized slot name">errorLine</weak_warning>></template>
    <template <error descr="Vue: Duplicate slot names found. ">v-slot:<weak_warning descr="Unrecognized slot name">errorLine</weak_warning></error>></template>
    <template #item.bar></template>
    <template <error descr="Vue: Duplicate slot names found. ">v-slot:item.bar</error>></template>
    <template #okSlot></template>
    <template <error descr="Vue: Duplicate slot names found. ">v-slot:okSlot</error>></template>
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

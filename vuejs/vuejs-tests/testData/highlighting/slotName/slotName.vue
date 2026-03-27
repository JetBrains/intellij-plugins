<template>
  <Foo>
    <template #errorLine></template>
    <template #okLine></template>
  </Foo>
  <Bar>
    <template #<weak_warning descr="Unrecognized slot name">errorLine</weak_warning>></template>
    <template #okLine></template>
  </Bar>
  <slot name="okLine"></slot>
  <<warning descr="Unknown html tag unknown-tag">unknown-tag</warning>>
    <!-- don't report problems for unknown tags -->
    <template #okLine></template>
  </<warning descr="Unknown html tag unknown-tag">unknown-tag</warning>>
</template>
<script>
import Foo from "./Foo"
export default {
  name:"Bar",
  components: {
    Foo
  }
}
</script>
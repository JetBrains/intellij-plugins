<template>
  <Foo>
    <template #errorLine></template>
    <template #okLine></template>
  </Foo>
  <Bar>
    <template #<warning descr="Unrecognized slot name">errorLine</warning>></template>
    <template #okLine></template>
  </Bar>
  <slot name="okLine"></slot>
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
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
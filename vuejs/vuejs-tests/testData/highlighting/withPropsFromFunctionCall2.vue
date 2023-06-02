<template>
  <span :class="{'add-me-if-true': foo, 'add-if-bar': bar}">
    Foo
  </span>
  <FooBar :foo="12" :bar="<error descr="Type  12  is not assignable to type  boolean | undefined   Type 12 is not assignable to type boolean">12</error>">

  </FooBar>
  <<warning descr="Element FooBar doesn't have required attribute foo">FooBar</warning>>

  </<warning descr="Element FooBar doesn't have required attribute foo">FooBar</warning>>
</template>
<script lang="ts">
import { defineComponent } from 'vue';

const useSomeProps = (<warning descr="Unused parameter foo">foo</warning>) => ({
  foo: {
    type: [String, Array, Object],
    required: true,
  },
  bar: {
    type: Boolean,
    default: true,
  },
});

export default defineComponent({
                                 name:"FooBar",
                                 props: useSomeProps('foobar'),
                               });
</script>
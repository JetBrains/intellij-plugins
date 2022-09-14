<template>
  <span :class="{'add-me-if-true': foo, 'add-if-bar': bar}">
    Foo
  </span>
  <FooBar :f<caret>oo="12" :bar="23">

  </FooBar>
</template>
<script lang="ts">
import { defineComponent } from 'vue';

const useSomeProps = (foo) => ({
  foo: {
    type: [String, Array, Object],
    required: true,
  },
});

export default defineComponent({
                                 name:"FooBar",
                                 props: {
                                   ...useSomeProps('foobar'),
                                   bar: {
                                     type: Boolean,
                                     default: true,
                                   },
                                 },
                               });
</script>
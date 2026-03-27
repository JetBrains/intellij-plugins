<template>
  <div>
    <p>{{ stories }}</p>
    <p>{{ <error descr="Unresolved variable or type fooBar">fooBar</error> }}</p>
    <div @input="getFoo()" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

function foo() {
  return {
    fooBar: 12
  }
}

export default defineComponent({
  setup: () => ({
    stories: 12,
    getFoo() {
      return 1;
    }
  })
});
</script>

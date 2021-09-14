<template>
  <div>
    <p>{{ stories }}</p>
    <p>{{ <weak_warning descr="Unresolved variable or type fooBar">fooBar</weak_warning> }}</p>
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

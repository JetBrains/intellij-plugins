<template>
  <div>
    <p>{{ stories }}</p>
    <p>{{ fooBar }}</p>
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

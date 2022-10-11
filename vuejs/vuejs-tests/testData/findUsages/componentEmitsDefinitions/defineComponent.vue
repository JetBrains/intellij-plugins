<template>
  <TestComponent @test-event=""></TestComponent>
</template>

<script>

import {defineComponent} from "vue";

export default defineComponent({
  name: "TestComponent",
  emits: ["test-event", "test-event-2"]
})
</script>


<script setup lang="ts">
import MyComponent from "./MyComponent.vue"

function wait(ms: number) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

<error>1 > "expect error"</error>;
const bar = await wait(100).then(() => "hello!");
</script>

<template>
  <MyComponent />
  {{bar}}
</template>

<script setup lang="ts">
import MyComponent from "./MyComponent";

function handleEvent(message: string) {
  alert(message)
}

function handleEventInvalid(message: { error: { text: string } }) {
  alert(message.error.text)
}
</script>

<template>
  <div>
    <MyComponent @custom="handleEvent"></MyComponent>
    <MyComponent @custom="handleEventInvalid"></MyComponent>
  </div>
</template>
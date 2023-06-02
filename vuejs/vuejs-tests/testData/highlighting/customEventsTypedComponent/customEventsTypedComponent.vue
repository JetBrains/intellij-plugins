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
    <MyComponent @custom="<error descr="Type (message: {    error: {        text: string    }}) => void is not assignable to type  (message: string) => void ">handleEventInvalid</error>"></MyComponent>
  </div>
</template>
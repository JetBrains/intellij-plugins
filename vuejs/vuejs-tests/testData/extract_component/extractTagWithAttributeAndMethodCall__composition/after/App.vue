<script setup>
import NewComponent from "./NewComponent.vue";

defineProps({
  one: {},
})

function compMethod() {
  // compute
}
</script>

<template>
  <NewComponent :comp-method="compMethod()" :one="one"/>
</template>

<script setup>
import NewComponent from "./NewComponent.vue";

defineProps({
  propWithCamel: {}
})
</script>

<template>
  <NewComponent :prop-with-camel="propWithCamel"/>
</template>

<script vapor>
import NewComponent from "./NewComponent.vue";

defineProps({
  one: {},
})
</script>

<template>
  <NewComponent :one="one"/>
</template>

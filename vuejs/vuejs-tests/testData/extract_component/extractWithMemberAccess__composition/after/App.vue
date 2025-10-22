<script setup>
import NewComponent from "./NewComponent.vue";

defineProps({
  item: {},
})
</script>

<template>
  <NewComponent :item="item"/>
</template>

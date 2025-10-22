<script vapor>
import NewComponent from "./NewComponent.vue";

defineProps({
  item: {},
})
</script>

<template>
  <NewComponent :item="item"/>
</template>

<script>
import NewComponent from "./NewComponent.vue";

export default {
  components: {NewComponent},
  props: {item: {}}
}
</script>

<template>
  <NewComponent :item="item"/>
</template>

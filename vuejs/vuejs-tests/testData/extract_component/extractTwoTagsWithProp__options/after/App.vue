<script>
import NewComponent from "./NewComponent.vue";

export default {
  name: 'existing',
  components: {NewComponent},
  props: {
    one: {}
  }
}
</script>

<template>
  <NewComponent :one="one"/>
</template>

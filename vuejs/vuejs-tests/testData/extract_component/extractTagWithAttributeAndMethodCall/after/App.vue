<template>
  <NewComponent :comp-method="compMethod()" :one="one"/>
</template>
<script>
import NewComponent from "./NewComponent.vue";

export default {
  components: {NewComponent},
  props: {
    one: {}
  },
  computed: {
    compMethod() {
    }
  }
}
</script>
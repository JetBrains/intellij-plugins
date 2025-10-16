<script>
import NewComponent from "./NewComponent.vue";

export default {
  components: {NewComponent},
  props: {
    propWithCamel: {}
  }
}
</script>

<template>
  <NewComponent :prop-with-camel="propWithCamel"/>
</template>

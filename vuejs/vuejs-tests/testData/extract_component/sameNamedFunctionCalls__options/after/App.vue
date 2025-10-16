<script>
import NewComponent from "./NewComponent.vue";

export default {
  components: {NewComponent},
  methods: {
    oneMore() {
    }
  }
}
</script>

<template>
  <NewComponent :one-more="oneMore"/>
</template>

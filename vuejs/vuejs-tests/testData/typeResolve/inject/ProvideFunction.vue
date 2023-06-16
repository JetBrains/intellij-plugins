<script>
import Inject from "./Inject.vue";

export default {
  name: "ProvideFunction",
  components: {Inject},
  provide() {
    return {
      func: false
    }
  }
}
</script>

<template>
  <Inject/>
</template>
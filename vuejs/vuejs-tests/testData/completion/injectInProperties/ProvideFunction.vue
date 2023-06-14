<script>
import Inject from "./InjectInProperties.vue";

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
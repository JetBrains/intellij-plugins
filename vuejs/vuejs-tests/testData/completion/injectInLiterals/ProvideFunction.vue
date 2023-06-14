<script>
import Inject from "./InjectInLiterals.vue";

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
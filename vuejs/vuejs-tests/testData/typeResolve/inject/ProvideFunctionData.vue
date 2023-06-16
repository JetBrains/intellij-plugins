<script>
import Inject from "./Inject.vue";

export default {
  name: "ProvideFunctionData",
  components: {Inject},
  data() {
    return {
      message: 'hello!'
    }
  },
  provide() {
    return {
      funcData: this.message
    }
  }
}
</script>

<template>
  <Inject/>
</template>
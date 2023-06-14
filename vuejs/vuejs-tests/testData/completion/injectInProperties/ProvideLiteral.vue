<script>
import Inject from "./InjectInProperties.vue";

export default {
  name: "ProvideLiteral",
  components: {Inject},
  provide: {
    literal: 'hello!'
  }
}
</script>

<template>
  <Inject/>
</template>
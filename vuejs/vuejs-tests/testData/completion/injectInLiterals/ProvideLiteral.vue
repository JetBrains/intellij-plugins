<script>
import Inject from "./InjectInLiterals.vue";

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
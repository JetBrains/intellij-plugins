<script>
import Inject from "./Inject.vue";

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
<script>
import Inject from "./InjectInLiteralsUnquoted.vue";

export default {
  name: "Provide",
  components: {Inject},
  provide: {
    provideLiteral: 'hello!'
  }
}
</script>

<template>
  <Inject/>
</template>
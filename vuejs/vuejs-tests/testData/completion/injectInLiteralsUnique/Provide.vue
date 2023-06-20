<script>
import Inject from "./InjectInLiteralsUnique.vue";

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
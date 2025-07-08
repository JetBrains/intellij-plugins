<script>
import Inject from "./injectInScriptSetupLiteralsUnquoted.vue";

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
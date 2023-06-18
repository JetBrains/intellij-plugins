<script>
import Inject from "./InjectInProperties.vue";

const myInjectionKey = Symbol()

export default {
  name: "ProvideSymbol",
  components: {Inject},
  provide: {
    [myInjectionKey]: 'hello!'
  }
}
</script>

<template>
  <Inject/>
</template>
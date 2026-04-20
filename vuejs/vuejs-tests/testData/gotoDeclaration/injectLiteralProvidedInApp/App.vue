<script>
import injectLiteralProvidedInApp from './injectLiteralProvidedInApp.vue'

export default {
  components: {injectLiteralProvidedInApp},
  provide: {
    message: 'hello!'
  },
}
</script>

<template>
  <injectLiteralProvidedInApp/>
</template>
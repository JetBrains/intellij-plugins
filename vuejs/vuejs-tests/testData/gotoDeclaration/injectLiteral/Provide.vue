<script>
import InjectLiteral from "./InjectLiteral.vue"

export default {
  components: {InjectLiteral},
  provide: {
    message: 'hello!'
  },
}
</script>

<template>
  <InjectLiteral/>
</template>
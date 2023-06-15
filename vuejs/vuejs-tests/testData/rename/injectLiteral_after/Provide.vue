<script>
import InjectLiteral from "./InjectLiteral.vue"

export default {
  components: {InjectLiteral},
  provide: {
    newName: 'hello!'
  },
}
</script>

<template>
  <InjectLiteral/>
</template>
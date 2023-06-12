<script>
import InjectAlias from "./InjectAlias.vue"

export default {
  components: {InjectAlias},
  provide() {
    return {
      message: 'msg'
    }
  }
}
</script>

<template>
  <InjectAlias/>
</template>
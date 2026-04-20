<script>
import InjectProperty from "./InjectProperty.vue"

export default {
  components: {InjectProperty},
  provide() {
    return {
      message: 'msg'
    }
  }
}
</script>

<template>
  <InjectProperty/>
</template>
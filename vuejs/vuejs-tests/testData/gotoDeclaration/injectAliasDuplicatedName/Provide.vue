<script>
import InjectAliasDuplicatedName from "./InjectAliasDuplicatedName.vue"

export default {
  components: {InjectAliasDuplicatedName},
  provide() {
    return {
      message: 'msg'
    }
  }
}
</script>

<template>
  <InjectAliasDuplicatedName/>
</template>
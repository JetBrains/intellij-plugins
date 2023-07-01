<script>
import {provide} from "vue";
import InjectSetup from "./InjectSetup.vue"

export default {
  components: {InjectSetup},
  setup() {
    if (a > b) {
      provide('injectSetup', 123)
    }
  }
}
</script>

<template>
  <InjectSetup/>
</template>
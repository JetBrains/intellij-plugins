<script>
import {provide} from "vue";
import Inject from "./InjectCallTypeEvaluation.vue"
import {myInjectionKey} from "./injectionKey";

export default {
  components: {Inject},
  setup() {
    provide('stringKey', 'abcd')
    provide(myInjectionKey, true)
  }
}
</script>

<template>
  <Inject/>
</template>
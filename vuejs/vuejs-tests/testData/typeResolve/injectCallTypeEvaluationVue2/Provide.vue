<script>
import {provide} from "vue";
import Inject from "./InjectCallTypeEvaluationVue2.vue"
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
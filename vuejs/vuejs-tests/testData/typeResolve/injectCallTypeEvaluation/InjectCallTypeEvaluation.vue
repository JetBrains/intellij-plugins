<script>
import {inject} from "vue";
import {myInjectionKey} from "./injectionKey";

export default {
  setup() {
    const stringInject = inject('stringKey')
    const keyInject = inject(myInjectionKey)

    return {
      stringInject,
      keyInject,
    }
  }
}
</script>

<template>
  {{ stringInject }}
  {{ keyInject }}
</template>
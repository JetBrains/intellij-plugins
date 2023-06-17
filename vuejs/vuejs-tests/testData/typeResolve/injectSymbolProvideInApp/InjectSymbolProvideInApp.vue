<script>
import {defineComponent} from 'vue'
import {myInjectionKey} from "./injectionKey";

export default defineComponent({
  name: "InjectSymbolProvideInApp",
  inject: {
    myLocalInject: {
      from: myInjectionKey
    }
  }
})
</script>

<template>
  {{ myLocalInject }}
  {{ myLocalInject.name }}
  {{ myLocalInject.lang }}
</template>
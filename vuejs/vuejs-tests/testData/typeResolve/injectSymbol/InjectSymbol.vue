<script lang="ts">
import {defineComponent} from 'vue'
import {myInjectionKey} from "./injectionKey";

export default defineComponent({
  name: "Inject",
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
</template>
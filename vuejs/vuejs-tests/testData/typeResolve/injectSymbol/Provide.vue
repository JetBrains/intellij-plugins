<script lang="ts">
import {defineComponent} from 'vue'
import {myInjectionKey} from "./injectionKey";
import Inject from "./InjectSymbol.vue";

export default defineComponent({
  components: {Inject},
  provide() {
    return {
      [myInjectionKey]: {
        name: 'bob'
      }
    }
  }
})
</script>

<template>
  <Inject/>
</template>

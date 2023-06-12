<script>
import { computed } from 'vue'
import Inject from "./Inject.vue";

export default {
  name: "ProvideComputed",
  components: {Inject},
  provide() {
    return {
      computed: computed(() => 123)
    }
  }
}
</script>

<template>
  <Inject/>
</template>
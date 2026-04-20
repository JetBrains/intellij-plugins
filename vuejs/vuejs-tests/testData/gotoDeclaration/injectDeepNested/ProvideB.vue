<script>
import ProvideC from "./ProvideC.vue";

export default {
  name: "ProvideB",
  components: {ProvideC},
  provide: {
    provideDeep: 123
  }
}
</script>

<template>
  <ProvideC/>
</template>
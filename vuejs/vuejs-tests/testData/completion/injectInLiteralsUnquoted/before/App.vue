<script>
import {defineComponent} from "vue";
import Provide from "./Provide.vue";

export default defineComponent({
  components: {Provide},
  provide: {
    provideInApp: 123,
  }
})

</script>

<template>
  <Provide/>
</template>

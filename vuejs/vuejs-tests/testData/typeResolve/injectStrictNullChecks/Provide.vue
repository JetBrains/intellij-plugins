<script lang="ts">
import {defineComponent} from 'vue'
import Inject from "./InjectStrictNullChecks.vue";

export default defineComponent({
  components: {Inject},
  provide: {
    message: "hello"
  }
})
</script>

<template>
  <Inject/>
</template>

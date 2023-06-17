<script lang="ts">
import {defineComponent} from 'vue'
import Inject from "./InjectWithDefault.vue";

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

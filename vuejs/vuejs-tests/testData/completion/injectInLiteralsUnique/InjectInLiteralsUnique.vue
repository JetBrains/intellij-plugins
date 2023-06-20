<script>
import {defineComponent} from "vue";

export default defineComponent({
  name: "InjectInLiteralsUnique",
  inject: [
    "provideGlobal",
    "provideInApp",
    "<caret>"
  ]
})
</script>

<template>
  {{ provideGlobal }}
  {{ provideInApp }}
</template>

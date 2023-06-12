<script>
import {defineComponent} from "vue";

export default defineComponent({
  name: "Inject",
  inject: ["literal", "func", "computed", "funcData", "app"]
})
</script>

<template>
  {{ literal }}
  {{ func }}
  {{ computed }}
  {{ funcData }}
  {{ app }}
</template>

<script>
import {defineComponent} from "vue";

export default defineComponent({
  name: "Inject",
  inject: [
    "literal",
    "func",
    "computed",
    "funcData",
    "app",
    "scriptSetup",
    "scriptSetupRef",
    "globalProvide",
    "globalProvideRef",
  ]
})
</script>

<template>
  {{ literal }}
  {{ func }}
  {{ computed }}
  {{ funcData }}
  {{ app }}
  {{ scriptSetup }}
  {{ scriptSetupRef }}
  {{ globalProvide }}
  {{ globalProvideRef }}
</template>

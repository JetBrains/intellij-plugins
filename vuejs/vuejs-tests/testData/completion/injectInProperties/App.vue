<script>
import {defineComponent} from "vue";
import ProvideLiteral from "./ProvideLiteral.vue";
import ProvideFunction from "./ProvideFunction.vue";
import ProvideComputed from "./ProvideComputed.vue";
import ProvideFunctionData from "./ProvideFunctionData.vue";
import ProvideScriptSetup from "./ProvideScriptSetup.vue";

export default defineComponent({
  components: {ProvideFunctionData, ProvideComputed, ProvideFunction, ProvideLiteral, ProvideScriptSetup},
  provide: {
    app: 123,
  }
})

</script>

<template>
  <ProvideLiteral/>
  <ProvideFunction/>
  <ProvideComputed/>
  <ProvideFunctionData/>
  <ProvideScriptSetup/>
</template>

<template>
  <define-emits @foo=""></define-emits>
  <define-component @test-event=""></define-component>
  <DefineEmits @foo="" @test-event=""></DefineEmits>
  <DefineComponent @foo="" @test-event=""></DefineComponent>
</template>
<script>
import DefineEmits from "./defineEmits"
import ExportComponent from "./exportComponent"
import DefineComponent from "./defineComponent"

export default {
  components: {DefineEmits, DefineComponent}
}
</script>
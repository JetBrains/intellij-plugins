<template>
  <define-emits @> </define-emits>
  <define-emits-with-type @> </define-emits-with-type>
  <export-component @></export-component>
  <define-component @></define-component>
</template>
<script>
import DefineEmits from "./defineEmits"
import ExportComponent from "./exportComponent"
import DefineComponent from "./defineComponent"
import DefineEmitsWithType from "./defineEmitsWithType"

export default {
  components: {DefineEmitsWithType, DefineEmits, ExportComponent, DefineComponent}
}
</script>
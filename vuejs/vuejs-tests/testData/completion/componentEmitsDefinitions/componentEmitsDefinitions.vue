<template>
  <define-emits @> </define-emits>
  <export-component @></export-component>
  <define-component @></define-component>
</template>
<script>
import DefineEmits from "./defineEmits"
import ExportComponent from "./exportComponent"
import DefineComponent from "./defineComponent"

export default {
  components: {DefineEmits, ExportComponent, DefineComponent}
}
</script>
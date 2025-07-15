<script setup lang="ts">
interface MyProps {
  optionsApiWithType: string
}
const props = defineProps<MyProps>()
</script>

<template>
  {{optionsApiWithType}}
  {{props.optionsApiWithType}}
  <define-props-interface-from-usage1 opt<caret>ions-api-with-type="foo"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 optionsApiWithType="foo"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 OptionsApiWithType="foo"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 :options-api-with-type="'foo'"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 :optionsApiWithType="'foo'"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 :OptionsApiWithType="'foo'"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 v-bind:options-api-with-type="'foo'"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 v-bind:optionsApiWithType="'foo'"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 v-bind:OptionsApiWithType="'foo'"></define-props-interface-from-usage1>
</template>

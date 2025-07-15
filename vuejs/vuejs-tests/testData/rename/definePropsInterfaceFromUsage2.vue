<script setup lang="ts">
interface MyProps {
  optionsApiWithType: string
}
const props = defineProps<MyProps>()
</script>

<template>
  {{opt<caret>ionsApiWithType}}
  {{props.optionsApiWithType}}
  <define-props-interface-from-usage2 options-api-with-type="foo"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 optionsApiWithType="foo"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 OptionsApiWithType="foo"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 :options-api-with-type="'foo'"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 :optionsApiWithType="'foo'"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 :OptionsApiWithType="'foo'"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 v-bind:options-api-with-type="'foo'"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 v-bind:optionsApiWithType="'foo'"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 v-bind:OptionsApiWithType="'foo'"></define-props-interface-from-usage2>
</template>

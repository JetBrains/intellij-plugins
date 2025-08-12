<script setup lang="ts">
defineProps({
  optionsApiWithType: {
    type: String,
    required: true
  }
})
</script>

<template>
  {{optionsApiWithType}}
  <define-props-object-literal-from-usage1 options-api-with-type="foo"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 optionsApiWithType="foo"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 OptionsApiWithType="foo"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 :opt<caret>ions-api-with-type="'foo'"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 :optionsApiWithType="'foo'"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 :OptionsApiWithType="'foo'"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 v-bind:options-api-with-type="'foo'"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 v-bind:optionsApiWithType="'foo'"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 v-bind:OptionsApiWithType="'foo'"></define-props-object-literal-from-usage1>
</template>

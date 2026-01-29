<script setup lang="ts">
defineProps({
  optionsApiWithType: {
    type: String,
    required: true
  }
})
</script>

<template>
  {{opt<caret>ionsApiWithType}}
  <define-props-object-literal-from-usage2 options-api-with-type="foo"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 optionsApiWithType="foo"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 OptionsApiWithType="foo"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 :options-api-with-type="'foo'"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 :optionsApiWithType="'foo'"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 :OptionsApiWithType="'foo'"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 v-bind:options-api-with-type="'foo'"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 v-bind:optionsApiWithType="'foo'"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 v-bind:OptionsApiWithType="'foo'"></define-props-object-literal-from-usage2>
</template>

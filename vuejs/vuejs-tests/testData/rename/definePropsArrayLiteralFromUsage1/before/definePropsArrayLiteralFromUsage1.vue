<script setup lang="ts">
defineProps(["optionsApiWithType"])
</script>

<template>
  {{optionsApiWithType}}
  <define-props-array-literal-from-usage1 opt<caret>ions-api-with-type="foo"></define-props-array-literal-from-usage1>
  <define-props-array-literal-from-usage1 optionsApiWithType="foo"></define-props-array-literal-from-usage1>
  <define-props-array-literal-from-usage1 OptionsApiWithType="foo"></define-props-array-literal-from-usage1>
  <define-props-array-literal-from-usage1 :options-api-with-type="'foo'"></define-props-array-literal-from-usage1>
  <define-props-array-literal-from-usage1 :optionsApiWithType="'foo'"></define-props-array-literal-from-usage1>
  <define-props-array-literal-from-usage1 :OptionsApiWithType="'foo'"></define-props-array-literal-from-usage1>
  <define-props-array-literal-from-usage1 v-bind:options-api-with-type="'foo'"></define-props-array-literal-from-usage1>
  <define-props-array-literal-from-usage1 v-bind:optionsApiWithType="'foo'"></define-props-array-literal-from-usage1>
  <define-props-array-literal-from-usage1 v-bind:OptionsApiWithType="'foo'"></define-props-array-literal-from-usage1>
</template>

<script setup lang="ts">
defineProps(["opt<caret>ionsApiWithType"])
</script>

<template>
  {{optionsApiWithType}}
  <define-props-array-literal-from-definition options-api-with-type="foo"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition optionsApiWithType="foo"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition OptionsApiWithType="foo"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition :options-api-with-type="'foo'"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition :optionsApiWithType="'foo'"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition :OptionsApiWithType="'foo'"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition v-bind:options-api-with-type="'foo'"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition v-bind:optionsApiWithType="'foo'"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition v-bind:OptionsApiWithType="'foo'"></define-props-array-literal-from-definition>
</template>

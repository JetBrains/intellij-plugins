<script setup lang="ts">
defineProps({
  opt<caret>ionsApiWithType: {
    type: String,
    required: true
  }
})
</script>

<template>
  {{optionsApiWithType}}
  <define-props-object-literal-from-definition options-api-with-type="foo"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition optionsApiWithType="foo"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition OptionsApiWithType="foo"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition :options-api-with-type="'foo'"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition :optionsApiWithType="'foo'"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition :OptionsApiWithType="'foo'"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition v-bind:options-api-with-type="'foo'"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition v-bind:optionsApiWithType="'foo'"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition v-bind:OptionsApiWithType="'foo'"></define-props-object-literal-from-definition>
</template>

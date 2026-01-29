<script setup lang="ts">
const props = defineProps<{
  optionsApiWithType: string
}>()
</script>

<template>
  {{opt<caret>ionsApiWithType}}
  <define-props-record-type-from-usage2 options-api-with-type="foo"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 optionsApiWithType="foo"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 OptionsApiWithType="foo"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 :options-api-with-type="'foo'"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 :optionsApiWithType="'foo'"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 :OptionsApiWithType="'foo'"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 v-bind:options-api-with-type="'foo'"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 v-bind:optionsApiWithType="'foo'"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 v-bind:OptionsApiWithType="'foo'"></define-props-record-type-from-usage2>
</template>

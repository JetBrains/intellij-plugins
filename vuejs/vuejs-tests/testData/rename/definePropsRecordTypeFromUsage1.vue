<script setup lang="ts">
const props = defineProps<{
  optionsApiWithType: string
}>()
</script>

<template>
  {{optionsApiWithType}}
  <define-props-record-type-from-usage1 opt<caret>ions-api-with-type="foo"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 optionsApiWithType="foo"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 OptionsApiWithType="foo"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 :options-api-with-type="'foo'"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 :optionsApiWithType="'foo'"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 :OptionsApiWithType="'foo'"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 v-bind:options-api-with-type="'foo'"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 v-bind:optionsApiWithType="'foo'"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 v-bind:OptionsApiWithType="'foo'"></define-props-record-type-from-usage1>
</template>

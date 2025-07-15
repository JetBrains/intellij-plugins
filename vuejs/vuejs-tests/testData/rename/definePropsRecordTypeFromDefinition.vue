<script setup lang="ts">
const props = defineProps<{
  opt<caret>ionsApiWithType: string
}>()
</script>

<template>
  {{optionsApiWithType}}
  <define-props-record-type-from-definition options-api-with-type="foo"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition optionsApiWithType="foo"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition OptionsApiWithType="foo"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition :options-api-with-type="'foo'"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition :optionsApiWithType="'foo'"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition :OptionsApiWithType="'foo'"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition v-bind:options-api-with-type="'foo'"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition v-bind:optionsApiWithType="'foo'"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition v-bind:OptionsApiWithType="'foo'"></define-props-record-type-from-definition>
</template>

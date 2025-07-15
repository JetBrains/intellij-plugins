<script setup lang="ts">
const props = defineProps<{
  newName: string
}>()
</script>

<template>
  {{ new<caret>Name }}
  <define-props-record-type-from-usage2 new-name="foo"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 newName="foo"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 OptionsApiWithType="foo"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 :new-name="'foo'"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 :newName="'foo'"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 :OptionsApiWithType="'foo'"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 v-bind:new-name="'foo'"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 v-bind:newName="'foo'"></define-props-record-type-from-usage2>
  <define-props-record-type-from-usage2 v-bind:OptionsApiWithType="'foo'"></define-props-record-type-from-usage2>
</template>

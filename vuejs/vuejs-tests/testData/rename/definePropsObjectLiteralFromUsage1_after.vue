<script setup lang="ts">
defineProps({
  newName: {
    type: String,
    required: true
  }
})
</script>

<template>
  {{ newName }}
  <define-props-object-literal-from-usage1 new-name="foo"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 newName="foo"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 OptionsApiWithType="foo"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 :new<caret>-name="'foo'"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 :newName="'foo'"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 :OptionsApiWithType="'foo'"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 v-bind:new-name="'foo'"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 v-bind:newName="'foo'"></define-props-object-literal-from-usage1>
  <define-props-object-literal-from-usage1 v-bind:OptionsApiWithType="'foo'"></define-props-object-literal-from-usage1>
</template>

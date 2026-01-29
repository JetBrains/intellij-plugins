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
  <define-props-object-literal-from-usage2 new-name="foo"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 newName="foo"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 OptionsApiWithType="foo"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 :new-name="'foo'"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 :newName="'foo'"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 :OptionsApiWithType="'foo'"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 v-bind:new-name="'foo'"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 v-bind:newName="'foo'"></define-props-object-literal-from-usage2>
  <define-props-object-literal-from-usage2 v-bind:OptionsApiWithType="'foo'"></define-props-object-literal-from-usage2>
</template>

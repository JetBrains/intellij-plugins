<script setup lang="ts">
defineProps(["new<caret>Name"])
</script>

<template>
  {{newName}}
  <define-props-array-literal-from-definition new-name="foo"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition newName="foo"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition OptionsApiWithType="foo"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition :new-name="'foo'"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition :newName="'foo'"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition :OptionsApiWithType="'foo'"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition v-bind:new-name="'foo'"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition v-bind:newName="'foo'"></define-props-array-literal-from-definition>
  <define-props-array-literal-from-definition v-bind:OptionsApiWithType="'foo'"></define-props-array-literal-from-definition>
</template>

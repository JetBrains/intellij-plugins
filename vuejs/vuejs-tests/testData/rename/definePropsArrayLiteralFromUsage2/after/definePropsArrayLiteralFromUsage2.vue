<script setup lang="ts">
defineProps(["newName"])
</script>

<template>
  {{newName}}
  <define-props-array-literal-from-usage2 new-name="foo"></define-props-array-literal-from-usage2>
  <define-props-array-literal-from-usage2 newName="foo"></define-props-array-literal-from-usage2>
  <define-props-array-literal-from-usage2 OptionsApiWithType="foo"></define-props-array-literal-from-usage2>
  <define-props-array-literal-from-usage2 :new-name="'foo'"></define-props-array-literal-from-usage2>
  <define-props-array-literal-from-usage2 :newName="'foo'"></define-props-array-literal-from-usage2>
  <define-props-array-literal-from-usage2 :OptionsApiWithType="'foo'"></define-props-array-literal-from-usage2>
  <define-props-array-literal-from-usage2 v-bind:new-name="'foo'"></define-props-array-literal-from-usage2>
  <define-props-array-literal-from-usage2 v-bind:newName="'foo'"></define-props-array-literal-from-usage2>
  <define-props-array-literal-from-usage2 v-bind:OptionsApiWithType="'foo'"></define-props-array-literal-from-usage2>
</template>

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
  <define-props-object-literal-from-definition new-name="foo"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition newName="foo"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition OptionsApiWithType="foo"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition :new-name="'foo'"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition :newName="'foo'"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition :OptionsApiWithType="'foo'"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition v-bind:new-name="'foo'"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition v-bind:newName="'foo'"></define-props-object-literal-from-definition>
  <define-props-object-literal-from-definition v-bind:OptionsApiWithType="'foo'"></define-props-object-literal-from-definition>
</template>

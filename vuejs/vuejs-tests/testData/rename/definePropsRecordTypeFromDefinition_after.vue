<script setup lang="ts">
const props = defineProps<{
  new<caret>Name: string
}>()
</script>

<template>
  {{ newName }}
  <define-props-record-type-from-definition new-name="foo"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition newName="foo"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition OptionsApiWithType="foo"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition :new-name="'foo'"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition :newName="'foo'"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition :OptionsApiWithType="'foo'"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition v-bind:new-name="'foo'"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition v-bind:newName="'foo'"></define-props-record-type-from-definition>
  <define-props-record-type-from-definition v-bind:OptionsApiWithType="'foo'"></define-props-record-type-from-definition>
</template>

<script setup lang="ts">
const props = defineProps<{
  newName: string
}>()
</script>

<template>
  {{ newName }}
  <define-props-record-type-from-usage1 new-name="foo"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 newName="foo"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 OptionsApiWithType="foo"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 :new-name="'foo'"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 :newName="'foo'"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 :OptionsApiWithType="'foo'"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 v-bind:new-name="'foo'"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 v-bind:newName="'foo'"></define-props-record-type-from-usage1>
  <define-props-record-type-from-usage1 v-bind:OptionsApiWithType="'foo'"></define-props-record-type-from-usage1>
</template>

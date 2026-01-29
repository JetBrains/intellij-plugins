<script setup lang="ts">
interface MyProps {
  newName: string
}
const props = defineProps<MyProps>()
</script>

<template>
  {{ newName }}
  {{ props.newName }}
  <define-props-interface-from-usage1 new-name="foo"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 newName="foo"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 OptionsApiWithType="foo"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 :new-name="'foo'"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 :newName="'foo'"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 :OptionsApiWithType="'foo'"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 v-bind:new-name="'foo'"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 v-bind:newName="'foo'"></define-props-interface-from-usage1>
  <define-props-interface-from-usage1 v-bind:OptionsApiWithType="'foo'"></define-props-interface-from-usage1>
</template>

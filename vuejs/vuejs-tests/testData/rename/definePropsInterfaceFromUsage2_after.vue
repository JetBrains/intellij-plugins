<script setup lang="ts">
interface MyProps {
  newName: string
}
const props = defineProps<MyProps>()
</script>

<template>
  {{ new<caret>Name }}
  {{ props.newName }}
  <define-props-interface-from-usage2 new-name="foo"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 newName="foo"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 OptionsApiWithType="foo"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 :new-name="'foo'"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 :newName="'foo'"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 :OptionsApiWithType="'foo'"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 v-bind:new-name="'foo'"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 v-bind:newName="'foo'"></define-props-interface-from-usage2>
  <define-props-interface-from-usage2 v-bind:OptionsApiWithType="'foo'"></define-props-interface-from-usage2>
</template>

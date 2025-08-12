<script setup lang="ts">
interface MyProps {
  opt<caret>ionsApiWithType: string
}
const props = defineProps<MyProps>()
</script>

<template>
  {{optionsApiWithType}}
  {{props.optionsApiWithType}}
  <define-props-interface-from-definition options-api-with-type="foo"></define-props-interface-from-definition>
  <define-props-interface-from-definition optionsApiWithType="foo"></define-props-interface-from-definition>
  <define-props-interface-from-definition OptionsApiWithType="foo"></define-props-interface-from-definition>
  <define-props-interface-from-definition :options-api-with-type="'foo'"></define-props-interface-from-definition>
  <define-props-interface-from-definition :optionsApiWithType="'foo'"></define-props-interface-from-definition>
  <define-props-interface-from-definition :OptionsApiWithType="'foo'"></define-props-interface-from-definition>
  <define-props-interface-from-definition v-bind:options-api-with-type="'foo'"></define-props-interface-from-definition>
  <define-props-interface-from-definition v-bind:optionsApiWithType="'foo'"></define-props-interface-from-definition>
  <define-props-interface-from-definition v-bind:OptionsApiWithType="'foo'"></define-props-interface-from-definition>
</template>

<script setup lang="ts">
import {MyProps} from "./fooProps"
const props = defineProps<MyProps>()
</script>

<template>
  {{opt<caret>ionsApiWithType}}
  {{props.optionsApiWithType}}
  <define-props-interface options-api-with-type="foo"></define-props-interface>
  <define-props-interface optionsApiWithType="foo"></define-props-interface>
  <define-props-interface OptionsApiWithType="foo"></define-props-interface>
  <define-props-interface :options-api-with-type="'foo'"></define-props-interface>
  <define-props-interface :optionsApiWithType="'foo'"></define-props-interface>
  <define-props-interface :OptionsApiWithType="'foo'"></define-props-interface>
  <define-props-interface v-bind:options-api-with-type="'foo'"></define-props-interface>
  <define-props-interface v-bind:optionsApiWithType="'foo'"></define-props-interface>
  <define-props-interface v-bind:OptionsApiWithType="'foo'"></define-props-interface>
</template>

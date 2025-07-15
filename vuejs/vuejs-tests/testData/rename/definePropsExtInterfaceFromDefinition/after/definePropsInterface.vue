<script setup lang="ts">
import {MyProps} from "./fooProps"
const props = defineProps<MyProps>()
</script>

<template>
  {{ newName }}
  {{ props.newName }}
  <define-props-interface new-name="foo"></define-props-interface>
  <define-props-interface newName="foo"></define-props-interface>
  <define-props-interface OptionsApiWithType="foo"></define-props-interface>
  <define-props-interface :new-name="'foo'"></define-props-interface>
  <define-props-interface :newName="'foo'"></define-props-interface>
  <define-props-interface :OptionsApiWithType="'foo'"></define-props-interface>
  <define-props-interface v-bind:new-name="'foo'"></define-props-interface>
  <define-props-interface v-bind:newName="'foo'"></define-props-interface>
  <define-props-interface v-bind:OptionsApiWithType="'foo'"></define-props-interface>
</template>

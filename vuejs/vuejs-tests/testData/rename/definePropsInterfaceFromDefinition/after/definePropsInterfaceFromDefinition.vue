<script setup lang="ts">
interface MyProps {
  newName: string
}
const props = defineProps<MyProps>()
</script>

<template>
  {{ newName }}
  {{ props.newName }}
  <define-props-interface-from-definition new-name="foo"></define-props-interface-from-definition>
  <define-props-interface-from-definition newName="foo"></define-props-interface-from-definition>
  <define-props-interface-from-definition OptionsApiWithType="foo"></define-props-interface-from-definition>
  <define-props-interface-from-definition :new-name="'foo'"></define-props-interface-from-definition>
  <define-props-interface-from-definition :newName="'foo'"></define-props-interface-from-definition>
  <define-props-interface-from-definition :OptionsApiWithType="'foo'"></define-props-interface-from-definition>
  <define-props-interface-from-definition v-bind:new-name="'foo'"></define-props-interface-from-definition>
  <define-props-interface-from-definition v-bind:newName="'foo'"></define-props-interface-from-definition>
  <define-props-interface-from-definition v-bind:OptionsApiWithType="'foo'"></define-props-interface-from-definition>
</template>

<script>
export default {
  name: "cmp",
  props: {
    NewName: {
      type: String,
      required: true
    }
  }
}
</script>

<template>
  <cmp options-api-with-type="foo"></cmp>
  <cmp optionsApiWithType="foo"></cmp>
  <cmp NewName="foo"></cmp>
  <cmp :options-api-with-type="'foo'"></cmp>
  <cmp :optionsApiWithType="'foo'"></cmp>
  <cmp :NewName="'foo'"></cmp>
  <cmp v-bind:options-api-with-type="'foo'"></cmp>
  <cmp v-bind:optionsApiWithType="'foo'"></cmp>
  <cmp v-bind:NewName="'foo'"></cmp>
</template>

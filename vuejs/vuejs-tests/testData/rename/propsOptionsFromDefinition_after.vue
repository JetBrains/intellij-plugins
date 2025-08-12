<script>
export default {
  name: "cmp",
  props: {
    new<caret>Name: {
      type: String,
      required: true
    }
  }
}
</script>

<template>
  {{ newName }}
  <cmp new-name="foo"></cmp>
  <cmp newName="foo"></cmp>
  <cmp OptionsApiWithType="foo"></cmp>
  <cmp :new-name="'foo'"></cmp>
  <cmp :newName="'foo'"></cmp>
  <cmp :OptionsApiWithType="'foo'"></cmp>
  <cmp v-bind:new-name="'foo'"></cmp>
  <cmp v-bind:newName="'foo'"></cmp>
  <cmp v-bind:OptionsApiWithType="'foo'"></cmp>
</template>

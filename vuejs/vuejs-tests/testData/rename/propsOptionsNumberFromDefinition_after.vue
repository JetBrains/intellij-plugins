<script>
export default {
  name: "cmp",
  props: {
    new<caret>Name23: {
      type: String,
      required: true
    }
  }
}
</script>

<template>
  <cmp new-name23="foo"></cmp>
  <cmp new-name-23="foo"></cmp>
  <cmp newName23="foo"></cmp>
  <cmp OptionsApiWithType23="foo"></cmp>
  <cmp :new-name23="'foo'"></cmp>
  <cmp :new-name-23="'foo'"></cmp>
  <cmp :newName23="'foo'"></cmp>
  <cmp :OptionsApiWithType23="'foo'"></cmp>
  <cmp v-bind:new-name23="'foo'"></cmp>
  <cmp v-bind:new-name-23="'foo'"></cmp>
  <cmp v-bind:newName23="'foo'"></cmp>
  <cmp v-bind:OptionsApiWithType23="'foo'"></cmp>
</template>

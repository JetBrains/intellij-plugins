<script>
export default {
  name: "cmp",
  props: {
    newName23: {
      type: String,
      required: true
    }
  }
}
</script>

<template>
  {{ new<caret>Name23 }}
  <cmp new-name23="foo"></cmp>
  <cmp newName23="foo"></cmp>
  <cmp OptionsApiWithType="foo"></cmp>
  <cmp :new-name23="'foo'"></cmp>
  <cmp :newName23="'foo'"></cmp>
  <cmp :OptionsApiWithType="'foo'"></cmp>
  <cmp v-bind:new-name23="'foo'"></cmp>
  <cmp v-bind:newName23="'foo'"></cmp>
  <cmp v-bind:OptionsApiWithType="'foo'"></cmp>
</template>

<script>
export default {
  name: "cmp",
  props: {
    opt<caret>ionsApiWithType23: {
      type: String,
      required: true
    }
  }
}
</script>

<template>
  <cmp options-api-with-type23="foo"></cmp>
  <cmp options-api-with-type-23="foo"></cmp>
  <cmp optionsApiWithType23="foo"></cmp>
  <cmp OptionsApiWithType23="foo"></cmp>
  <cmp :options-api-with-type23="'foo'"></cmp>
  <cmp :options-api-with-type-23="'foo'"></cmp>
  <cmp :optionsApiWithType23="'foo'"></cmp>
  <cmp :OptionsApiWithType23="'foo'"></cmp>
  <cmp v-bind:options-api-with-type23="'foo'"></cmp>
  <cmp v-bind:options-api-with-type-23="'foo'"></cmp>
  <cmp v-bind:optionsApiWithType23="'foo'"></cmp>
  <cmp v-bind:OptionsApiWithType23="'foo'"></cmp>
</template>

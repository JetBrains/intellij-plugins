<script>
export default {
  name: "cmp",
  props: {
    optionsApiWithType: {
      type: String,
      required: true
    }
  }
}
</script>

<template>
  {{ optionsApiWithType }}
  <cmp options-api-with-type="foo"></cmp>
  <cmp options<caret>ApiWithType="foo"></cmp>
  <cmp OptionsApiWithType="foo"></cmp>
  <cmp :options-api-with-type="'foo'"></cmp>
  <cmp :optionsApiWithType="'foo'"></cmp>
  <cmp :OptionsApiWithType="'foo'"></cmp>
  <cmp v-bind:options-api-with-type="'foo'"></cmp>
  <cmp v-bind:optionsApiWithType="'foo'"></cmp>
  <cmp v-bind:OptionsApiWithType="'foo'"></cmp>
</template>

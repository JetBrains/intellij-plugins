<script>
import MyComponent from "./MyComponent.vue"
export default {
  name: "foo",
  components: {
    MyComponent
  }
}
</script>

<template>
  <my-component new-name23="foo"></my-component>
  <my-component newName23="foo"></my-component>
  <my-component OptionsApiWithType="foo"></my-component>
  <my-component :new-name23="'foo'"></my-component>
  <my-component :newName23="'foo'"></my-component>
  <my-component :OptionsApiWithType="'foo'"></my-component>
  <my-component v-bind:new-name23="'foo'"></my-component>
  <my-component v-bind:newName23="'foo'"></my-component>
  <my-component v-bind:OptionsApiWithType="'foo'"></my-component>
</template>

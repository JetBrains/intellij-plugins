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
  <my-component new-name="foo"></my-component>
  <my-component newName="foo"></my-component>
  <my-component OptionsApiWithType="foo"></my-component>
  <my-component :new-name="'foo'"></my-component>
  <my-component :newName="'foo'"></my-component>
  <my-component :OptionsApiWithType="'foo'"></my-component>
  <my-component v-bind:new-name="'foo'"></my-component>
  <my-component v-bind:newName="'foo'"></my-component>
  <my-component v-bind:OptionsApiWithType="'foo'"></my-component>
</template>

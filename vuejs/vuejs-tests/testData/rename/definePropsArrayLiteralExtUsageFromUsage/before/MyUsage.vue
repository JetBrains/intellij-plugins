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
  <my-component options-api-with-type="foo"></my-component>
  <my-component optionsApiWithType="foo"></my-component>
  <my-component OptionsApiWithType="foo"></my-component>
  <my-component :options-api-with-type="'foo'"></my-component>
  <my-component :optionsApiWithType="'foo'"></my-component>
  <my-component :OptionsApiWithType="'foo'"></my-component>
  <my-component v-bind:opti<caret>ons-api-with-type="'foo'"></my-component>
  <my-component v-bind:optionsApiWithType="'foo'"></my-component>
  <my-component v-bind:OptionsApiWithType="'foo'"></my-component>
</template>

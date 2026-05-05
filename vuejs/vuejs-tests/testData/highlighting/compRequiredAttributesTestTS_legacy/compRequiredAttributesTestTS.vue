<template>
  <div id="app">
    <<warning descr="Element camelCase doesn't have required attribute one"><warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning></warning> v-bind='<error descr="Unresolved variable or type incorrect">incorrect</error>'></<warning descr="Element camelCase doesn't have required attribute one"><warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning></warning>>
  <<warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning> :one="5"></<warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning>>
<camelCase one="test" two="2" three=3 with-camel-case="1" four=1></camelCase>
<camelCase one="test" v-bind:two="2" :three=3 withCamelCase="1"></camelCase>
<<warning descr="Element camelCase doesn't have required attribute three">camelCase</warning> v-bind:incorrect='0' v-bind='input'></<warning descr="Element camelCase doesn't have required attribute three">camelCase</warning>>
<camelCase v-bind:three='3' v-bind='input'></camelCase>
<camelCase one="test" v-bind:two="2" :three=3 withCamelCase="1" not-required=11></camelCase>
<<warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning> one="test" v-bind:two="2" :three=3></<warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning>>
</div>
</template>
<script lang='ts'>
import {Component, Prop} from "vue-property-decorator"

interface Foo {
  one: string,
  two: string,
  withCamelCase: boolean
}
@Component({
  name: 'camelCase',
  props: {
    one: {required:true},
    withCamelCase: {required:true},
  }
})
export default class MyComponent {
  input: Foo

  @Prop({required: true}) two
  @Prop({required: true}) three
  @Prop({required: false}) notRequired

}
</script>
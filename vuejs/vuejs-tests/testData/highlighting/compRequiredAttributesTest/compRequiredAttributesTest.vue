<template>
  <div id="app">
    <<warning descr="Element camelCase doesn't have required attribute one"><warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning></warning> v-bind='<weak_warning descr="Unresolved variable or type incorrect">incorrect</weak_warning>'></<warning descr="Element camelCase doesn't have required attribute one"><warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning></warning>>
    <<warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning> :one="5"></<warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning>>
    <camelCase one="test" two="2" three=3 with-camel-case="1" four=1></camelCase>
    <camelCase one="test" v-bind:two="2" :three=3 withCamelCase="1"></camelCase>
    <<warning descr="Element camelCase doesn't have required attribute three">camelCase</warning> v-bind:incorrect='0' v-bind='input'></<warning descr="Element camelCase doesn't have required attribute three">camelCase</warning>>
    <camelCase v-bind:three='3' v-bind='input'></camelCase>
    <camelCase one="test" v-bind:two="2" :three=3 withCamelCase="1" not-required=11></camelCase>
    <<warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning> one="test" v-bind:two="2" :three=3></<warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning>>
  </div>
</template>
<script>
export default {
  name: 'camelCase',
  props: {
    one: {required:true},
    two: {required:true},
    three: {required:true},
    withCamelCase: {required:true},
    notRequired: {required:false}
  },
  data () {
    return {
      input: {
        one: '',
        two: '',
        withCamelCase: ''
      }
    }
  }
}
</script>
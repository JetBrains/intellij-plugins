<template>
  <for-v-bind :class="2" v-bind:style="<error descr="Expression expected">"</error> :test-prop.camel="1" v-on:click="callMe" @copy="onCopy" ></for-v-bind>
  <for-v-bind class="" style="" v-on:submit.prevent></for-v-bind>
  <div <warning descr="Attribute @ is not allowed here">@</warning>="<weak_warning descr="Unresolved variable or type foo">foo</weak_warning>"></div>
  <div <warning descr="Attribute : is not allowed here">:</warning>="<weak_warning descr="Unresolved variable or type foo">foo</weak_warning>"></div>
</template>

<script>
export default {
  name: "for-v-bind",
  props: {
    testProp: {}
  },
  methods: {
    callMe() {},
    onCopy() {}
  }
}
</script>
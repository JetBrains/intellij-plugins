<template>
  <div id="scroller" v-on:click="foo = <error descr="Assigned expression type  12  is not assignable to type  boolean ">12</error>"></div>
  <div id="scrollers" v-on:click="onScrolled"></div>
  {{12 + foo}}
  {{bar}}
</template>

<script lang="ts">
  import {Component, Vue} from 'vue-property-decorator'

  export default
  @Component({})
  class Index extends <error descr="No base constructor has the specified number of type arguments">Vue</error> {

    constructor(props, private bar: boolean, private <warning descr="Unused field unusedBar">unusedBar</warning>) {
      super(props)
    }

    private onScrolled() {

    }

    private <warning descr="Unused method unusedFun">unusedFun</warning>() {

    }

    private get <warning descr="Unused property unusedGetter">unusedGetter</warning>() {

    }

    private get foo(): boolean {
      return true
    }

    private set <warning descr="Unused property unusedSetter">unusedSetter</warning>(<warning descr="Unused parameter a">a</warning>) {

    }

    private set foo(a: boolean) {
      a.toString();
    }
  }
</script>

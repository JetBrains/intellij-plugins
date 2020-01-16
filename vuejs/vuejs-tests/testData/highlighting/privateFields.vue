<template>
  <div id="scroller" v-on:click="foo = <weak_warning descr="Assigned expression type number is not assignable to type boolean">12</weak_warning>"></div>
  <div id="scrollers" v-on:click="onScrolled"></div>
  {{12 + foo}}
  {{bar}}
</template>

<script lang="ts">
  import {Component, Vue} from 'vue-property-decorator'

  <warning descr="Unused default export">export default</warning>
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

    private get foo(): number {
      return 12
    }

    private set <warning descr="Unused property unusedSetter">unusedSetter</warning>(<warning descr="Unused parameter a">a</warning>) {

    }

    private set foo(a: <error descr="Set accessor method has type that is not compatible with get accessor type, expecting 'number'">boolean</error>) {
      a.toString();
    }
  }
</script>

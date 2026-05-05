<template>
  <div :title="'foo' | localFilter(12)"></div>
  <div :title="false | localFilter('ss','ss')"></div>
  <div :title="true | localFilter('str', 12) | localFilter2('sss', 12)"></div>
  <div :title="true | badFilter"></div>
  <div :title="12 | unknownFilter(123)"></div>
  <div :title="'foo' | get()"/>
</template>

<script lang="ts">
  import Component from "vue-property-decorator";

  @Component({
    filters: {
      localFilter: function (arg1: boolean, arg2: string, arg3: number): number {
        return (arg1 + arg2 + arg3) as number
      },
      localFilter2: function (arg1: boolean, arg2: string, arg3: number) {
        return arg1 + arg2 + arg3
      },
      badFilter: function (): string {
        return ""
      },
      get: (<warning descr="Unused parameter value">value</warning>: string, <warning descr="Unused parameter path">path</warning>: string) => {
        return ""
      }
    }
  })
  export default class Hello {

  }
</script>

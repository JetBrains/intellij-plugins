<template>
  <div :title="'foo' | localFilter<error descr="Invalid number of filter arguments, expected 2">(12)</error>"></div>
  <div :title="false | localFilter('ss',<error descr="Argument type  \"ss\"  is not assignable to parameter type  number ">'ss'</error>)"></div>
  <div :title="<error descr="Argument type  number  is not assignable to parameter type  boolean ">true | localFilter('str', 12)</error> | localFilter2('sss', 12)"></div>
  <div :title="true | <error descr="Filter function should accept at least one argument">badFilter</error>"></div>
  <div :title="12 | <error descr="Unresolved filter unknownFilter">unknownFilter</error>(123)"></div>
  <div :title="'foo' | get<error descr="Invalid number of filter arguments, expected 1">()</error>"/>
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

<template>
  <div>
    <div>
      <div>
        Hello
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  export abstract class Greeter<T> {
    greetNTimes(to, {from, times}: { from: string[], times: number }) {
        return <error>range</error>(times).map(item => <warning>{return this.greet(to, from)<warning></warning>}</warning>)<EOLWarning></EOLWarning>
    }

    abstract greet(to: string, from: string[]): T<EOLWarning></EOLWarning>
  }

  export class ConsoleGreeter extends Greeter<string> {
    greet(to: string, from: string[]): string {
        return "Hello, " + to + " from " + from.join(<warning>',<caret>'</warning>)<EOLWarning></EOLWarning>
    }
  }

  var anotherArrow = () => {
      return {x: 1}<EOLWarning></EOLWarning>
  }<EOLWarning></EOLWarning>
</script>
<style>
  div {
    color: red
  }
</style>
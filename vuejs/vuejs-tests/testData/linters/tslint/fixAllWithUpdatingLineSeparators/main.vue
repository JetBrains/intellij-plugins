<template>
  <div>
    <div>
      <div>
        Hello
      </div>
    </div>
  </div>
</template>

<script lang="ts"><EOLWarning descr="tslint: Expected linebreak to be 'CRLF' (linebreak-style)"></EOLWarning>
<warning descr="tslint: Expected linebreak to be 'CRLF' (linebreak-style)">export abstract class Greeter<T> {</warning>
<warning>    greetNTimes(to, {from, times}: { from: string[], times: number }) {</warning>
<warning>        return <error>range</error>(times).map(item => <warning>{return this.greet(to, from)<warning></warning>}</warning>)</warning><EOLWarning></EOLWarning>
<warning>    }</warning>
<EOLWarning></EOLWarning>
<warning>    abstract greet(to: string, from: string[]): T</warning><EOLWarning></EOLWarning>
<warning>}</warning>
<EOLWarning></EOLWarning>
<warning>export class ConsoleGreeter extends Greeter<string> {</warning>
<warning>    greet(to: string, from: string[]): string {</warning>
<warning>        return "Hello, " + to + " from " + from.join(<warning>',<caret>'</warning>)</warning><EOLWarning></EOLWarning>
<warning>    }</warning>
<warning>}</warning>
<EOLWarning></EOLWarning>
<EOLWarning></EOLWarning><EOLWarning>
</EOLWarning><warning>  </warning>
<warning>  </warning>
<warning>var anotherArrow = () => {</warning>
<warning>    return {x: 1}</warning><EOLWarning></EOLWarning>
<warning descr="tslint: Expected linebreak to be 'CRLF' (linebreak-style)">}</warning><EOLWarning descr="tslint: Missing semicolon (semicolon)"></EOLWarning>
</script>
<style>
  div {
    color: red
  }
</style>
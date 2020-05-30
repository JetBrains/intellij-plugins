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
        return range(times).map(item =>this.greet(to, from));
    }

    abstract greet(to: string, from: string[]): T;
}

export class ConsoleGreeter extends Greeter<string> {
    greet(to: string, from: string[]): string {
        return "Hello, " + to + " from " + from.join(",");
    }
}

var anotherArrow = () => {
    return {x: 1};
};
</script>
<style>
  div {
    color: red
  }
</style>
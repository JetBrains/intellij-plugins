<script vapor lang="ts">
import StandardLabel from './StandardLabel.vue';
</script>

<template>
  <main>
    <StandardLabel :label="$t1('aaa', 'bbb')"/>
    <StandardLabel :label="$t1(13, 42)"/>
    <StandardLabel :label="$t1(4n, 8n)"/>
  </main>

  <main>
    <StandardLabel :label="$t1('aaa', 'bbb', <error descr="TS2554: Expected 2 arguments, but got 3.">16</error>)"/>
    <StandardLabel :label="<error descr="TS2769: No overload matches this call.
  Overload 1 of 3, '(a: string, b: string): string', gave the following error.
    Argument of type 'number' is not assignable to parameter of type 'string'.
  Overload 2 of 3, '(a: number, b: number): string', gave the following error.
    Argument of type 'boolean' is not assignable to parameter of type 'number'.
  Overload 3 of 3, '(a: bigint, b: bigint): string', gave the following error.
    Argument of type 'number' is not assignable to parameter of type 'bigint'.">$t1</error>(13, false)"/>
    <StandardLabel :label="<error descr="TS2554: Expected 2 arguments, but got 1.">$t1</error>(4n)"/>
  </main>

  <main>
    <StandardLabel>{{ $t1('aaa', 'bbb') }}</StandardLabel>
    <StandardLabel>{{ $t1(13, 42) }}</StandardLabel>
    <StandardLabel>{{ $t1(4n, 8n) }}</StandardLabel>
  </main>

  <main>
    <StandardLabel>{{ $t1('aaa', 'bbb', <error descr="TS2554: Expected 2 arguments, but got 3.">16</error>) }}</StandardLabel>
    <StandardLabel>{{ <error descr="TS2769: No overload matches this call.
  Overload 1 of 3, '(a: string, b: string): string', gave the following error.
    Argument of type 'number' is not assignable to parameter of type 'string'.
  Overload 2 of 3, '(a: number, b: number): string', gave the following error.
    Argument of type 'boolean' is not assignable to parameter of type 'number'.
  Overload 3 of 3, '(a: bigint, b: bigint): string', gave the following error.
    Argument of type 'number' is not assignable to parameter of type 'bigint'.">$t1</error>(13, false) }}</StandardLabel>
    <StandardLabel>{{ <error descr="TS2554: Expected 2 arguments, but got 1.">$t1</error>(4n) }}</StandardLabel>
  </main>
</template>

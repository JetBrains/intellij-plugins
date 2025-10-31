<script setup lang="ts">
let <error descr="TS2322: Type 'number' is not assignable to type 'string'."><error descr="TS6133: 'a' is declared but its value is never read.">a</error></error>: string = 1;

function acceptNumber(num: number): number { return num; }

acceptNumber(<error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>);
</script>

<template>
  <div v-text="acceptNumber(<error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>)" />
  <div>{{acceptNumber(<error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>)}}</div>
</template>

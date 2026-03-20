<script setup lang="ts">
export function hello(p: number, p2: number) {
  console.log(p2);
}
let <error descr="TS2322: Type 'number' is not assignable to type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">a</error>: string = 1;
</script>

<template>
</template>

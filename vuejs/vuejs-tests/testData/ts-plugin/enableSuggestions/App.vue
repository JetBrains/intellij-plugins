<script setup lang="ts">
export function hello(<weak_warning descr="TS6133: 'p' is declared but its value is never read." textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES">p</weak_warning>: number, p2: number) {
console.log(p2);
}
let <error descr="TS2322: Type 'number' is not assignable to type 'string'." textAttributesKey="ERRORS_ATTRIBUTES"><error descr="TS6133: 'a' is declared but its value is never read." textAttributesKey="ERRORS_ATTRIBUTES">a</error></error>: string = 1;
</script>

<template>
</template>

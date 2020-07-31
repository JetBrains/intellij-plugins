<template><div></div></template>
<script lang="ts">
    declare var React;
    export let <error descr="TS2322: Type '1' is not assignable to type 'string'.">a</error>:string = 1;
    
    function test() {
       return <<error descr="TS2304: Cannot find name 'div'.">div</error>><error descr="TS2304: Cannot find name 'Hello'.">Hello</error></<error descr="TS1161: Unterminated regular expression literal."></error>div>
    }
</script>

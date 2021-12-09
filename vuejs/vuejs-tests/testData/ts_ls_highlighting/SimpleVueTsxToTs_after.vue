<template><div></div></template>
<script lang="ts">
    declare var React;
    export let <error>a</error>:string = 1;
    
    function test() {
       return <<error descr="TS2304: Cannot find name 'div'.">div</error>><error descr="TS2304: Cannot find name 'Hello'.">Hello</error></<error descr="TS1161: Unterminated regular expression literal."></error>div>
    }
</script>

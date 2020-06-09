<template><div></div></template>
<script lang="ts">
    let a = 1;

    <error descr="TS2322: Type '\"\"' is not assignable to type 'number'.">a</error> = "";
<<error descr="TS2304: Cannot find name 'script'.">script</error>><EOLError descr="expression expected"></EOLError><EOLError descr="TS1109: Expression expected."></EOLError><EOLError descr="Element script is not closed"></EOLError>
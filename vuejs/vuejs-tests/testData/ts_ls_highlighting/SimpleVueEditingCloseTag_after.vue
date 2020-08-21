<template><div></div></template>
<script lang="ts">
    let a = 1;

    <error>a</error> = "";
<<error descr="TS2304: Cannot find name 'script'.">script</error>><EOLError descr="Expression expected"></EOLError><EOLError descr="TS1109: Expression expected."></EOLError><EOLError descr="Element script is not closed"></EOLError>
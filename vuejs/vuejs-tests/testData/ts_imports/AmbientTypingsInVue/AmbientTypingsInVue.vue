

<template><div></div></template>
<script lang="ts">
import * as Bar2 from 'myModule';

new Bar2.Bar2().do2();
new Bar2.Bar2().<error>do1</error>();

globalVar;
<error>globalVar1</error>;

</script>
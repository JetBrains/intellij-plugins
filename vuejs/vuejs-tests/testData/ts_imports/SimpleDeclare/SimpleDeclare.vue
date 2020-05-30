<template><div></div></template>
<script lang="ts">
//no errors
import { MyMod } from 'my-mod';

new MyMod().do1();
new MyMod().<error>do2</error>();
let z: MyMod;
z.do1();
z.<error>do2</error>();
 
</script>

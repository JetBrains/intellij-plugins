<template><div></div></template>
<script lang="ts">
import { FileImported } from "@/fileImported";
new FileImported().do1();
new FileImported().<error>do2</error>();

//no errors
<error>ClassToImp<caret>ort</error>


</script>

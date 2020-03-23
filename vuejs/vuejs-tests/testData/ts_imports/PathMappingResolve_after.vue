<template><div></div></template>
<script lang="ts">
import { FileImported } from "@/fileImported";
import {ClassToImport} from "@/fileToImport";
new FileImported().do1();
new FileImported().<error>do2</error>();

//no errors
ClassToImport


</script>

<template><div></div></template>
<script lang="ts">
  let z:Test;
  z.prop1;
  z.<error>prop2</error>;
  
  let z2:<error descr="Corresponding file is not included in tsconfig.json">Test2</error>;
 
</script>

<template><div></div></template>
<script lang="ts">
import {Hello} from './enum'
 
export default {
   accept(e: Hello) {
      if (e ==<caret><error descr="Expression expected"> </error>  ) {}
   }
}
</script>

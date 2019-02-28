<template>
 <div>
   <div>
     <!--  after replacement with whitespace in service, this should violate some rules   -->
     <div>
       Hello
     </div>
   </div>
 </div> 
</template>

<script lang="ts">
  export default {
    name: <warning><caret>"main"</warning>
  }
</script>
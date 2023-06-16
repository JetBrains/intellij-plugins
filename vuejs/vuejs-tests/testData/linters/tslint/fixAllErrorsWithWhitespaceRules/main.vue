<template>
 <div>
   <div>
     <div>
       Hello
     </div>
   </div>
 </div> 
</template>

<script lang="ts">
  export default {
    name: <warning><caret>"main"</warning>
  }<EOLWarning></EOLWarning>
  const <weak_warning>z</weak_warning> = () => <warning>{return {foo: 1}<warning></warning>}</warning><EOLWarning></EOLWarning>
</script>
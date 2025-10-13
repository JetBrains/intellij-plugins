<script vapor lang="ts">
</script>

<template>
  <MyButtonFromPlugin v-my-mutate="console.warn"></MyButtonFromPlugin>
  <MyButtonFromPlugin v-my-mutate.attr.sub="console.warn"></MyButtonFromPlugin>
  <MyLabelFromPlugin v-my-intersect.once="console.warn"></MyLabelFromPlugin>
  
  <!-- invalid modifier -->
  <MyLabelFromPlugin v-my-intersect.<warning descr="Unrecognized modifier">passive</warning>="console.warn"></MyLabelFromPlugin>
</template>

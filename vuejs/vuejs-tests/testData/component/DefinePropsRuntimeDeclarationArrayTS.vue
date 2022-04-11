<!--
Expected Component Model:
Prop(name=optional, required=false, presentableType=null)
Prop(name=required, required=false, presentableType=null)
-->
<script setup lang="ts">
defineProps(["optional", "required"]);
</script>

<template>
  {{optional}}
  {{required}}
</template>

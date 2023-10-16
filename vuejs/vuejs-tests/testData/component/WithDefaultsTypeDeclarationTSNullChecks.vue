<script setup lang="ts">
withDefaults(defineProps<{
  prop1: number,
  prop2: number,
  prop3?: number,
  prop4?: number,
}>(), {
  prop2: 1,
  prop3: 1,
});

prop1; // TODO Should be unresolved
</script>

<template>
  <<warning descr="Element WithDefaultsTypeDeclarationTSNullChecks doesn't have required attribute prop1">WithDefaultsTypeDeclarationTSNullChecks</warning> />
  <WithDefaultsTypeDeclarationTSNullChecks :prop1="1" />
  <!-- todo below, possibly only :prop1="undefined" should be an error in strictNullChecks=true -->
  <WithDefaultsTypeDeclarationTSNullChecks :prop1="<error descr="Type undefined is not assignable to type number">undefined</error>" :prop2="<error descr="Type undefined is not assignable to type number">undefined</error>" :prop3="undefined" :prop4="undefined" />
  <WithDefaultsTypeDeclarationTSNullChecks :prop1="1" :prop2="1" :prop3="1" :prop4="1" />
  <WithDefaultsTypeDeclarationTSNullChecks :prop1="prop1" :prop2="prop2" :prop3="prop3" :prop4="prop4" />

  {{prop1}}
  {{prop2}}
  {{prop3}}
  {{prop4}}
</template>

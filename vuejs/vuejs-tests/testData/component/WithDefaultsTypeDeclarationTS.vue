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
  <<warning descr="Element WithDefaultsTypeDeclarationTS doesn't have required attribute prop1">WithDefaultsTypeDeclarationTS</warning> />
  <WithDefaultsTypeDeclarationTS :prop1="1" />
  <WithDefaultsTypeDeclarationTS :prop1="undefined" :prop2="undefined" :prop3="undefined" :prop4="undefined" />
  <WithDefaultsTypeDeclarationTS :prop1="1" :prop2="1" :prop3="1" :prop4="1" />
  <WithDefaultsTypeDeclarationTS :prop1="prop1" :prop2="prop2" :prop3="prop3" :prop4="prop4" />

  {{prop1}}
  {{prop2}}
  {{prop3}}
  {{prop4}}
</template>

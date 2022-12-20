<script setup lang="ts">
const {
  prop2 = 1,
  prop3 = 1,
  prop4: aliased,
  ...rest
} = defineProps<{
  prop1: number,
  prop2: number,
  prop3?: number,
  prop4?: number,
}>();

<error descr="Unresolved variable or type prop1">prop1</error>;
rest.prop1;
rest.<error descr="Unresolved variable prop2">prop2</error>;
<error descr="Unresolved variable or type prop4">prop4</error>;
aliased;
</script>

<template>
  <<warning descr="Element PropsDestructureTypeDeclarationTS doesn't have required attribute prop1">PropsDestructureTypeDeclarationTS</warning> />
  <PropsDestructureTypeDeclarationTS :prop1="1" />
  <PropsDestructureTypeDeclarationTS :prop1="undefined" :prop2="undefined" :prop3="undefined" :prop4="undefined" />
  <PropsDestructureTypeDeclarationTS :prop1="1" :prop2="1" :prop3="1" :prop4="1" />
  <PropsDestructureTypeDeclarationTS :prop1="prop1" :prop2="prop2" :prop3="prop3" :prop4="prop4" />

  {{prop1}}
  {{prop2}}
  {{prop3}}
  {{prop4}}
  {{aliased}}
  {{rest.prop1}}
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  prop1: number,
  prop2: number,
  prop3?: number,
  prop4?: number,
}>(), {
  prop2: 1,
  prop3: 1,
});

props.prop1;
<error descr="Unresolved variable or type prop1">prop1</error>;
</script>

<template>
  <<warning descr="Element WithDefaultsTypeDeclarationWithAssignmentTS doesn't have required attribute prop1">WithDefaultsTypeDeclarationWithAssignmentTS</warning> />
  <WithDefaultsTypeDeclarationWithAssignmentTS :prop1="1" />
  <!-- todo below, :prop1="undefined" should be an error in strictNullChecks=true -->
  <WithDefaultsTypeDeclarationWithAssignmentTS :prop1="undefined" :prop2="undefined" :prop3="undefined" :prop4="undefined" />
  <WithDefaultsTypeDeclarationWithAssignmentTS :prop1="1" :prop2="1" :prop3="1" :prop4="1" />
  <WithDefaultsTypeDeclarationWithAssignmentTS :prop1="prop1" :prop2="prop2" :prop3="prop3" :prop4="prop4" />

  {{prop1}}
  {{prop2}}
  {{prop3}}
  {{prop4}}
  {{props.prop1}}
</template>

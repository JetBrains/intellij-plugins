<script setup>
const {
  prop2 = 1,
  prop3 = 1,
  prop4: aliased,
  ...rest
} = defineProps({
  prop1: { type: Number, required: true },
  prop2: { type: Number, required: true },
  prop3: Number,
  prop4: Number,
});

<weak_warning descr="Unresolved variable or type prop1">prop1</weak_warning>;
rest.prop1;
rest.prop2;
<weak_warning descr="Unresolved variable or type prop4">prop4</weak_warning>;
aliased;
</script>

<template>
  <<warning descr="Element PropsDestructureRuntimeDeclarationJS doesn't have required attribute prop1">PropsDestructureRuntimeDeclarationJS</warning> />
  <PropsDestructureRuntimeDeclarationJS :prop1="1" />
  <PropsDestructureRuntimeDeclarationJS :prop1="undefined" :prop2="undefined" :prop3="undefined" :prop4="undefined" />
  <PropsDestructureRuntimeDeclarationJS :prop1="1" :prop2="1" :prop3="1" :prop4="1" />
  <PropsDestructureRuntimeDeclarationJS :prop1="prop1" :prop2="prop2" :prop3="prop3" :prop4="prop4" />

  {{prop1}}
  {{prop2}}
  {{prop3}}
  {{prop4}}
  {{aliased}}
  {{rest.prop1}}
</template>

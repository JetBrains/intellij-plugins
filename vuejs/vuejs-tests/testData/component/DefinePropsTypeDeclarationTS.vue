<script setup lang="ts">
defineProps<{
  optional?: string,
  required: string,
  requiredNullish: string|undefined,
}>();

optional; // TODO Should be unresolved
</script>

<template>
  <<warning descr="Element DefinePropsTypeDeclarationTS doesn't have required attribute required-nullish">DefinePropsTypeDeclarationTS</warning>
    v-if="false"
    :required="'something'"
    :optional="'something'"
  />
  {{optional}}
  {{required}}
  {{requiredNullish}}
</template>

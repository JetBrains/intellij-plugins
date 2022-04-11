<!--
Expected Component Model:
Prop(name=optional, required=false, presentableType=string | undefined)
Prop(name=required, required=true, presentableType=string)
Prop(name=requiredNullish, required=true, presentableType=string | undefined)
-->
<script setup lang="ts">
defineProps<{
  optional?: string,
  required: string,
  requiredNullish: string|undefined,
}>();
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

<!--
Expected Component Model:
Prop(name=optional, required=false, presentableType=string | undefined)
Prop(name=required, required=true, presentableType=string)
-->
<script setup lang="ts">
type Props = {
  optional?: string,
  required: string,
}

defineProps<Props>();
</script>

<template>
  {{optional}}
  {{required}}
</template>

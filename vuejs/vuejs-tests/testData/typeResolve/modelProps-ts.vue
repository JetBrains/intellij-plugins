<script setup lang="ts">
import type {PropType} from "vue";

defineModel<string>({required: false}) // string | undefined

defineModel('numDefault', {
  type: Number as PropType<123 | 234>,
  default: 345,
  required: false,
}) // 123 | 234

defineModel('nameRequired', {
  type: String,
  required: true,
}) // string

defineModel<string>('nameRequiredGeneric', {
  required: true,
}) // string
</script>

<template>
  <div>{{ modelValue }}</div>
  <div>{{ numDefault }}</div>
  <div>{{ nameRequired }}</div>
  <div>{{ nameRequiredGeneric }}</div>
</template>
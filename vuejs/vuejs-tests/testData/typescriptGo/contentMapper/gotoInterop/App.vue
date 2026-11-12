<script lang="ts">
export function formatLabel(label: string): string {
  return label.toUpperCase()
}
</script>

<script setup lang="ts">
import { formatValue } from "./utils"
</script>

<template>
  <span>{{ formatValue("hello") }}</span>
</template>
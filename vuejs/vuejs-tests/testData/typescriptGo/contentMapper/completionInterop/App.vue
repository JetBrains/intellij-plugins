<script lang="ts">
export function formatLabel(label: string): string {
  return label.toUpperCase()
}
</script>

<script setup lang="ts">
import { ref } from "vue"
import { double } from "./utils"

const count = ref(1)
double(count.value)
</script>

<template>
  <span>{{ count.toF }}</span>
</template>
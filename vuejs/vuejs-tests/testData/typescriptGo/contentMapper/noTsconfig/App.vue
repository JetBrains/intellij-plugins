<script lang="ts">
export function formatLabel(label: string): string {
  return label.toUpperCase()
}
</script>

<script setup lang="ts">
import { ref } from "vue"
import { double } from "./utils"

const count = ref(1)
const <error descr="TS2322: Type 'number' is not assignable to type 'string'.">doubled</error>: string = double(count.value)
</script>

<template>
  <button @click="count++">{{ formatLabel(String(count)) }}</button>
  <span>{{ double(<error descr="TS2345: Argument of type 'string' is not assignable to parameter of type 'number'.">"2"</error>) }} {{ doubled }}</span>
  <span>{{ <error>missing</error> }}</span>
</template>

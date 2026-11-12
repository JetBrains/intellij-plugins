<script lang="ts">
export const appTitle = "App"
</script>

<script setup lang="ts">
import { host, port } from "./config.foo"

const <error descr="TS2322: Type 'number' is not assignable to type 'string'.">portText</error>: string = port
</script>

<template>
  <span>{{ host.toUpperCase() }} {{ port.<error descr="TS2339: Property 'toUpperCase' does not exist on type '8080'.">toUpperCase</error>() }} {{ portText }}</span>
</template>

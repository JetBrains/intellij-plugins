<script setup lang="ts">
import { ref } from <error descr="TS2307: Cannot find module 'vue' or its corresponding type declarations.">"vue"</error>

const count = ref(1)
const <error descr="TS2322: Type 'number' is not assignable to type 'string'.">label</error>: string = 1
</script>

<template>
  <span>{{ count }} {{ label }}</span>
</template>

<script setup lang="ts">
const <error descr="TS2322: Type 'string' is not assignable to type 'number'.">title</error>: number = "Other"
</script>

<template>
  <h1>{{ title.toFixed() }} {{ <error>unknownName</error> }}</h1>
</template>

<script setup lang="ts">
let <error>dummyErrorToSatisfyTestFramework</error>: string = 1;

/**
 * docs have to be removed!
 */
let local = 323 as number | string;
</script>

<template>
  <div v-if="typeof local === 'number'">
    <div v-text="<caret>local.toFixed()"/>
  </div>
</template>
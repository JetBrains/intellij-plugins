<script setup lang="ts">
let <error>dummyErrorToSatisfyTestFramework</error>: string = 1;

/**
 * docs have to be removed!
 */
let local = 323 as number | null;
</script>

<template>
  <div v-text="<caret>local"></div>
</template>
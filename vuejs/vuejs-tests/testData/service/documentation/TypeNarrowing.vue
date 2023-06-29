<script setup lang="ts">

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
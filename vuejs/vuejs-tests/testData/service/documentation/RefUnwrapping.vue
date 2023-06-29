<script setup lang="ts">
import {ref} from "vue";

/**
 * docs have to be removed!
 */
let local = ref(323 as number | string);
</script>

<template>
  <div v-text="<caret>local"></div>
</template>
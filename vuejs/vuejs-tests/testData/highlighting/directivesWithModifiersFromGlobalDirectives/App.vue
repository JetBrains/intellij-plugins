<script setup lang="ts">
import StandardButton from './StandardButton.vue'
import StandardLabel from './StandardLabel.vue'
</script>

<template>
  <main v-my-click-outside.once>
    <StandardButton></StandardButton>
    <StandardLabel></StandardLabel>
  </main>
</template>

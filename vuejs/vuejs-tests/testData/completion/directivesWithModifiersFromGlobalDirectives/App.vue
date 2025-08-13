<script setup lang="ts">
import StandardButton from './StandardButton.vue'
import StandardLabel from './StandardLabel.vue'
</script>

<template>
  <main v-my-click-outside.once>
    <StandardButton></StandardButton>
    <StandardLabel></StandardLabel>
  </main>

  <main v-my-intersect.once>
    <StandardButton></StandardButton>
    <StandardLabel></StandardLabel>
  </main>

  <main v-my-mutate.once>
    <StandardButton></StandardButton>
    <StandardLabel></StandardLabel>
  </main>
</template>

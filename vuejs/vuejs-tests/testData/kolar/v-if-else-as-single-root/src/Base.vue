<script setup lang="ts">
const show = true
</script>

<template>
  <div v-if="show" class="on">On</div>
  <span v-else class="off">Off</span>
</template>
